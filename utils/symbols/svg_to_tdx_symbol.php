#!/usr/bin/php
<?php

/**
 * Converts a SVG file to TopoDroidX's symbol drawing language.
 * 
 * It only supports circles and paths. All other element types are silently ignored.
 * 
 * Only the outline of the elements are considered, all other attributes are 
 * silently ignored.
 * 
 * If you used any other shape in the SVG file, convert it to paths before trying 
 * to use this script.
 * 
 * It's possible to set the amount of decimal places to be generated but the default 
 * of 1 seems to be really good.
 * 
 * Usage:
 * 
 * 1. Create you symbol definition in the vector art software of your choice.
 * 
 * 2. Export it as a SVG file.
 * 
 * 3. Convert the file using this script.
 * 
 * 4. Copy the output of this script to the 'path' section inside the TopoDroidX
 *    symbol definition.
 * 
 * 5. Enjoy your new TopoDroid symbol!
 * 
 * Observations:
 * 
 * 1. The viewBox of the SVG file is used to set the scaling factor to the 
 *    (-10,-10) x (10,10) TopoDroids canvas. SVGs viewBox attribute is usually 
 *    set by the paper size or document properties in vector art software. 
 *    Prefer square viewBoxes!
 * 
 * 2. If the viewbox is not a square, the smaller dimension (width or height)
 *    is increased to match the other so shapes are not distorted.
 * 
 * 3. Have fun creating TopoDroid symbols!
 * 
 * 4. Remember to share your new symbols!!
 * 
 */

 class Point 
 {
     private $x;
     private $y;
 
     public function __constructor () {}
 
     public static function fromString(string $aString) 
     {
         $result = Point::_validString($aString);
         if ($result === false) 
         {
             return false;
         }
         $instance = new self();
         $instance->setValues($result['x'], $result['y']);
         return $instance;        
     }
 
     public static function fromValues($aX, $aY) 
     {
         $instance = new self();
         $instance->setValues($aX, $aY);
         return $instance;        
     }
 
     public static function fromPoint(Point $aPoint) 
     {
         $instance = new self();
         $instance->setValues($aPoint->x, $aPoint->y);
         return $instance;        
     }
 
     private static function _validString($aString) 
     {
         $result = preg_split('/[\s,]+/', trim($aString));
         if (!is_array($result) || 
             (count($result) != 2) ||
             !is_numeric($result[0]) ||
             !is_numeric($result[1])) 
         {
             return false;
         }
 
         return [
             'x' => (float) $result[0],
             'y' =>(float) $result[1]
         ];
     }
 
     public function setFromString($aString) 
     {
         $result = Point::_validString($aString);
         if ($result === false) 
         {
             return false;
         }
 
         $this->x = $result['x'];
         $this->y = $result['y'];
 
         return true;
     }
 
     public function setValues($aX, $aY) 
     {
         $this->x = (float) $aX;
         $this->y = (float) $aY;       
     }
 
     public function setFromPoint(Point $aPoint) 
     {
         $this->x = $aPoint->x;
         $this->y = $aPoint->y;
     }
 
     public function __get($aName) 
     {
         return (float) $this->$aName;
     }
 
     public function __set($aName, $aValue) 
     {
         $this->$aName = (float) $aValue;
     }
 }

class SVGToTDXSymbolConverter 
{
    private const CONTROL_POINT_TYPE_NONE = 0;
    private const CONTROL_POINT_TYPE_CUBIC = 1;
    private const CONTROL_POINT_TYPE_QUADRATIC = 2;
    private const INDENT = '  ';

    private $_file;
    private $_xml;

    private $_tdxTop = -10;
    private $_tdxBottom = 10;
    private $_tdxLeft = -10;
    private $_tdxRight = 10;

    private $_svgTop;
    private $_svgBottom;
    private $_svgLeft;
    private $_svgRight;

    private $_aX;
    private $_bX;
    private $_aY;
    private $_bY;

    private $_tdx;
    private Point $_firstPoint;
    private $_firstPointSet = false;
    private Point $_lastPoint;
    private $_pathData;
    private $_pathDataIndex;
    private Point $_lastControlPoint;
    private $_lastControlPointType = SVGToTDXSymbolConverter::CONTROL_POINT_TYPE_NONE;

    private $_decimalPlaces = 1;

    public function __construct()
    {
        $this->_firstPoint = new Point();
        $this->_lastPoint = new Point();
        $this->_lastControlPoint = new Point();
    }

    public function setViewBoxFromString($aViewBox)
    {
        $result = preg_split('/[\s,]+/', trim($aViewBox));
        if (!is_array($result) || (count($result) != 4)) 
        {
            echo "Need 4 numeric values in viewBox attribute to initialize converter: '$aViewBox'. ABORTING CONVERSION\n";
            return false;
        }

        $minX = (float) $result[0]; 
        $minY = (float) $result[1]; 
        $width = (float) $result[2];
        $height = (float) $result[3];

        # Preventing distortions resulting from non-square view boxes.
        if ($width > $height)
        {
            $height = $width;
        }
        elseif ($height > $width)
        {
            $width = $height;
        }

        $this->_svgTop = $minY;
        $this->_svgBottom = $minY + $height;
        $this->_svgLeft = $minX;
        $this->_svgRight = $minX + $width;

        $this->_calculateFactors();

        return true;
    }

    public function convertFile($aFile)
    {
        if (!file_exists($aFile))
        {
            printf(
                "File '%s' does not exist. ABORTING CONVERSION\n",
                $aFile
            ); 
            return false;
        }

        $this->_file = $aFile;
        $this->_xml = simplexml_load_file($aFile);

        if ($this->_xml === false) 
        {
            printf(
                "XML parsing of file '%s' failed. ABORTING CONVERSION\n",
                $aFile
            ); 
            return false;
        }

        If (!$this->_xml->registerXPathNamespace('svg', 'http://www.w3.org/2000/svg'))
        {
            printf(
                "XPath namespace registering for file '%s' failed. ABORTING CONVERSION\n",
                $aFile
            ); 
            return false;
        }

        $viewBox = trim($this->_xml->attributes()->{'viewBox'});
        if (!$this->setViewBoxFromString($viewBox))
        {
            printf(
                "ViewBox parsing for file '%s' failed. ABORTING CONVERSION\n",
                $aFile
            ); 
            return false;
        }

        // Find all the <path> elements with a 'd' attribute
        // $paths = $xml->xpath('/svg:svg//svg:path[@d]');
        $convertableElements = $this->_xml->xpath(' /svg:svg//svg:path[@d] | /svg:svg//svg:circle');
        // print_r($convertableElements);

        $this->_tdx = '';

        if (empty($convertableElements)) 
        {
            printf(
                "No convertable elements found in the SVG file '%s'.\n",
                $aFile
            );
        } else {
            foreach ($convertableElements as $element) 
            {
                $elementName = $element->getName();
                // printf("Elemento name '%s'\n", $element->getName());
                switch ($elementName)
                {
                    case 'circle':
                        $this->_convertCircle($element);
                        break;
                    case 'path':
                        $this->_convertPath($element);
                        break;
                    default:
                        printf(
                            "Unsupported element type '%s'. SKIPPNG!!\n", 
                            $elementName
                        );
                }
            }
        }

        return $this->_tdx;
    }

    private function _convertCircle($aCircle)
    {
        // print_r($aCircle);
        if (isset($aCircle['cx'])) {
            $cx = (float) $aCircle->attributes()->{'cx'};
            // printf("cx: '%s'\n", $cx);
        } else {
            printf(
                "Circle '%s' has no cx attribute. SKIPPING!!\n",
                $aCircle
            );
            return false;
        }

        if (isset($aCircle['cy'])) {
            $cy = (float) $aCircle->attributes()->{'cy'};
            // printf("cy: '%s'\n", $cy);
        } else {
            printf(
                "Circle '%s' has no cy attribute. SKIPPING!!\n",
                $aCircle
            );
            return false;
        }

        if (isset($aCircle['r'])) {
            $r = (float) $aCircle->attributes()->{'r'};
            // printf("r: '%s'\n", $r);
        } else {
            printf(
                "Circle '%s' has no r attribute. SKIPPING!!\n",
                $aCircle
            );
            return false;
        } 

        $center = Point::fromValues($cx, $cy);
        $convertedCenter = $this->_getConvertedPoint($center);

        $convertedRadius = $this->_getConvertedLength($r);
        
        $this->_tdx .= sprintf(
            "%saddCircle %s %s\n", 
            self::INDENT, 
            $this->_pointToString($convertedCenter),
            $this->_getFormatedNumber($convertedRadius)
        );
    }

    private function _getFormatedNumber($aNumber)
    {
        $formated = number_format($aNumber, $this->_decimalPlaces, '.', '');
        $formated = $this->_removeTrailingZeroes($formated);
        return $formated;
    }

    private function _pointToString($aPoint) 
    {
        $aString = sprintf(
            '%s %s', 
            $this->_getFormatedNumber($aPoint->x), 
            $this->_getFormatedNumber($aPoint->y)
        );
        return $aString;
    }

    private function _removeTrailingZeroes($aNumberString) 
    {
        if ($this->_decimalPlaces == 0) 
        {
            return $aNumberString;
        }
        while (strlen($aNumberString) > 0) 
        {
            $rightMost = substr($aNumberString, -1, 1);
            if ($rightMost == '0') 
            {
                $aNumberString = substr($aNumberString, 0, -1);
                continue;
            } 
            elseif ($rightMost == '.') 
            {
                $aNumberString = substr($aNumberString, 0, -1);
                break;
            }
            else 
            {
                break;
            }
        }
        return $aNumberString;
    }

    function setDecimalPlaces($aDecimalPlaces) 
    {
        $this->_decimalPlaces = (int) $aDecimalPlaces;
    }

    function getDecimalPlaces() 
    {
        return (int) $this->_decimalPlaces;
    }

    private function _calculateFactors() 
    {
        $this->_aX = ($this->_tdxRight - $this->_tdxLeft) / 
            ($this->_svgRight - $this->_svgLeft);
        $this->_bX = $this->_tdxLeft - ($this->_svgLeft * $this->_aX);
        $this->_aY = ($this->_tdxBottom - $this->_tdxTop) / 
            ($this->_svgBottom - $this->_svgTop);
        $this->_bY = $this->_tdxTop - ($this->_svgTop * $this->_aY);
    }

    private function _convertPath($aPath) 
    {
        $this->_firstPointSet = false;
        $this->_lastPoint->setValues(0, 0);

        $this->_pathData = $aPath->attributes()->{'d'};
        $this->_pathData = strtr($this->_pathData, "\n", ' ');
        $this->_pathData = trim($this->_pathData);

        $this->_pathDataIndex = 0;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $command = substr($this->_pathData, $this->_pathDataIndex, 1);
            $this->_pathDataIndex++;
            switch ($command) 
            {
                case 'M':
                    $this->_moveTo(false);
                    break;
                case 'm':
                    $this->_moveTo(true);
                    break;
                case 'L':
                    $this->_lineTo(false);
                    break;
                case 'l':
                    $this->_lineTo(true);
                    break;
                case 'H':
                    $this->_horizontalLine(false);
                    break;
                case 'h':
                    $this->_horizontalLine(true);
                    break;
                case 'V':
                    $this->_verticalLine(false);
                    break;
                case 'v':
                    $this->_verticalLine(true);
                    break;
                case 'Z':
                case 'z':
                    $this->_closingLine();
                    break;
                case 'C':
                    $this->_cubicTo(false);
                    break;
                case 'c':
                    $this->_cubicTo(true);
                    break;
                case 'S':
                    $this->_smoothCubic(false);
                    break;
                case 's':
                    $this->_smoothCubic(true);
                    break;
                case 'Q':
                    $this->_quadratic(false);
                    break;
                case 'q':
                    $this->_quadratic(true);
                    break;
                case 'T':
                    $this->_smoothQuadratic(false);
                    break;
                case 't':
                    $this->_smoothQuadratic(true);
                    break;
                case 'A':
                    $this->_arc(false);
                    break;
                case 'a':
                    $this->_arc(true);
                    break;
                default:
                    $this->_consumeNumbers();
                    break;
            }
        }
        return $this->_tdx;
    }

    private function _arc($isRelative)
    {
        $first = true;
        $isLine = false;
        while ($this->_pathDataIndex < strlen($this->_pathData))
        {
            $rx = $this->_getNumber();
            if ($rx === false) 
            {
                if ($first)
                {
                    printf(
                        "*** Failure reading x radiius for arc from: '%s' - SKIPPING!!\n",
                        $this->_getRestPathData()
                    );
                }
                return false;
            }
            if ($rx < 0)
            {
                $rx = -$rx;
            }
            elseif ($rx == 0)
            {
                $isLine = true;
            }

            $ry = $this->_getNumber();
            if ($ry === false) 
            {
                printf(
                    "*** Failure reading y radiius for arc from: '%s' - SKIPPING!!\n",
                    $this->_getRestPathData()
                );
                return false;
            }
            if ($ry < 0)
            {
                $ry = -$ry;
            }
            elseif ($ry == 0)
            {
                $isLine = true;
            }

            $rotation = $this->_getNumber();
            if ($rotation === false) 
            {
                printf(
                    "*** Failure reading rotation for arc from: '%s' - SKIPPING!!\n",
                    $this->_getRestPathData()
                );
                return false;
            }

            $flagArc = $this->_getNumber();
            if ($flagArc === false) 
            {
                printf(
                    "*** Failure reading large arc flag for arc from: '%s' - SKIPPING!!\n",
                    $this->_getRestPathData()
                );
                return false;
            }
            $flagArc = ($flagArc == 0) ? 0 : 1;

            $flagSweep = $this->_getNumber();
            if ($flagSweep === false) 
            {
                printf(
                    "*** Failure reading sweep flag for arc from: '%s' - SKIPPING!!\n",
                    $this->_getRestPathData()
                );
                return false;
            }
            $flagSweep = ($flagSweep == 0) ? 0 : 1;

            $pointEnd = $this->_parsePoint($isRelative, 'arc');
            if ($pointEnd === false) 
            {
                printf(
                    "*** Failure reading end point for arc from: '%s' - SKIPPING!!\n",
                    $this->_getRestPathData()
                );
                return false;
            }

            $first = false;

            if ($isLine)
            {
                $this->_actionFromPoint('lineTo', $pointEnd);
                $this->_lastPoint->setFromPoint($pointEnd);
                continue;
            }

            $pointStart = Point::fromPoint($this->_lastPoint);

            $cubics = arcToBezier(
                $pointStart->x,
                $pointStart->y,
                $pointEnd->x,
                $pointEnd->y,
                $rx,
                $ry,
                $rotation,
                $flagArc,
                $flagSweep,
            );
            // echo "Cubics:\n";
            // print_r($cubics);
            foreach ($cubics as $cubic)
            {
                $control1 = Point::fromValues($cubic['x1'], $cubic['y1']);
                $control2 = Point::fromValues($cubic['x2'], $cubic['y2']);
                $cubicEnd = Point::fromValues($cubic['x'], $cubic['y']);
                $this->_completeCubicTo($control1, $control2, $cubicEnd);
            }
        }
    }

    private function _smoothQuadratic($isRelative)
    {
        $command = 'quadratic';
        $first = true;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $qControl1 = $this->_getReflectionControlPoint(SVGToTDXSymbolConverter::CONTROL_POINT_TYPE_QUADRATIC);

            $pointEnd = $this->_parsePoint($isRelative, $command);
            if ($pointEnd === false) 
            {
                if ($first)
                {
                    printf(
                        "*** Failure reading endpoint coordinates for '%s' from: '%s' - SKIPPING!!\n",
                        $command,
                        $this->_getRestPathData()
                    );
                }
                return false;
            }

            $this->_completeQuadratic($qControl1, $pointEnd);

            $first = false;
        }
    }

    private function _quadratic($isRelative) 
    {
        $command = 'quadratic';
        $first = true;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $qControl1 = $this->_parsePoint($isRelative, $command);
            if ($qControl1 === false) 
            {
                if ($first) 
                {
                    printf(
                        "*** Failure reading point 1 coordinates for '%s' from: '%s' - SKIPPING!!\n",
                        $command,
                        $this->_getRestPathData()
                    );
                }
                return;
            }

            $pointEnd = $this->_parsePoint($isRelative, $command);
            if ($pointEnd === false) 
            {
                printf(
                    "*** Failure reading endpoint coordinates for '%s' from: '%s' - SKIPPING!!\n",
                    $command,
                    $this->_getRestPathData()
                );
                return false;
            }

            $this->_completeQuadratic($qControl1, $pointEnd);

            $first = false;
        }
    }

    private function _completeQuadratic($qControl1, $pointEnd)
    {
        # Creating cubic Bézier control points from one quadratic Bézier control point
        # Formula from https://stackoverflow.com/questions/3162645/convert-a-quadratic-bezier-to-a-cubic-one
        $xc1 = $this->_lastPoint->x + 
            ((2 * ($qControl1->x - $this->_lastPoint->x)) / 3);
        $yc1 = $this->_lastPoint->y + 
            ((2 * ($qControl1->y - $this->_lastPoint->y)) / 3);
        $xc2 = $pointEnd->x + 
            ((2 * ($qControl1->x - $pointEnd->x)) / 3);
        $yc2 = $pointEnd->y + 
            ((2 * ($qControl1->y - $pointEnd->y)) / 3);
        $control1 = Point::fromValues($xc1, $yc1);
        $control2 = Point::fromValues($xc2, $yc2);

        $this->_completeCubicTo($control1, $control2, $pointEnd);

        $this->_lastControlPoint->setFromPoint($qControl1);
        $this->_lastControlPointType = SVGToTDXSymbolConverter::CONTROL_POINT_TYPE_QUADRATIC;
    }

    private function _getReflectionControlPoint($aControlPointType)
    {
        if ($this->_lastControlPointType == $aControlPointType)
        {
            $newX = $this->_lastPoint->x + 
                ($this->_lastPoint->x - $this->_lastControlPoint->x);
            $newY = $this->_lastPoint->y + 
                ($this->_lastPoint->y - $this->_lastControlPoint->y);
            $newPoint = Point::fromValues($newX, $newY);
        }
        else
        {
            $newPoint = Point::fromPoint($this->_lastPoint); 
        }

        return $newPoint;
    }

    private function _smoothCubic($isRelative)
    {
        $aAction = 'cubicTo';
        $first = true;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $control1 = $this->_getReflectionControlPoint(SVGToTDXSymbolConverter::CONTROL_POINT_TYPE_CUBIC);

            $control2 = $this->_parsePoint($isRelative, $aAction);
            if ($control2 === false) 
            {
                if ($first) 
                {
                    printf(
                        "*** Failure reading point 2 coordinates for '%s' from: '%s' - SKIPPING!!\n",
                        $aAction,
                        $this->_getRestPathData()
                    );
                }
                return;
            }
            
            $pointEnd = $this->_parsePoint($isRelative, $aAction);
            if ($pointEnd === false) 
            {
                printf(
                    "*** Failure reading endpoint coordinates for '%s' from: '%s' - SKIPPING!!\n",
                    $aAction,
                    $this->_getRestPathData()
                );
                return false;
            }
            
            if ($this->_completeCubicTo($control1, $control2, $pointEnd))
            {
                $first = false;
            }
            else
            {
                break;
            }
        }  
    }

    private function _cubicTo($isRelative) 
    {
        $aAction = 'cubicTo';
        $first = true;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $control1 = $this->_parsePoint($isRelative, $aAction);
            if ($control1 === false) 
            {
                if ($first) 
                {
                    printf(
                        "*** Failure reading point 1 coordinates for '%s' from: '%s' - SKIPPING!!\n",
                        $aAction,
                        $this->_getRestPathData()
                    );
                }
                return;
            }

            $control2 = $this->_parsePoint($isRelative, $aAction);
            if ($control2 === false) 
            {
                printf(
                    "*** Failure reading point 2 coordinates for '%s' from: '%s' - SKIPPING!!\n",
                    $aAction,
                    $this->_getRestPathData()
                );
                return;
            }

            $pointEnd = $this->_parsePoint($isRelative, $aAction);
            if ($pointEnd === false) 
            {
                printf(
                    "*** Failure reading endpoint coordinates for '%s' from: '%s' - SKIPPING!!\n",
                    $aAction,
                    $this->_getRestPathData()
                );
                return false;
            }

            $this->_completeCubicTo($control1, $control2, $pointEnd);

            $first = false;

            $this->_lastControlPoint->setFromPoint($control2);
            $this->_lastControlPointType = SVGToTDXSymbolConverter::CONTROL_POINT_TYPE_CUBIC;
        }        
    }

    private function _completeCubicTo($control1, $control2, $pointEnd)
    {
        $converted1 = $this->_getConvertedPoint($control1);
        $converted2 = $this->_getConvertedPoint($control2);
        $convertedEnd = $this->_getConvertedPoint($pointEnd);

        $this->_tdx .= sprintf(
            "%s%s %s %s %s\n", 
            self::INDENT, 
            'cubicTo',
            $this->_pointToString($converted1),
            $this->_pointToString($converted2),
            $this->_pointToString($convertedEnd)
        );

        $this->_lastPoint->setFromPoint($pointEnd);
    }

    private function _closingLine() 
    {
        $this->_actionFromPoint('lineTo', $this->_firstPoint);
    }

    private function _verticalLine($isRelative) 
    {
        $first = true;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $newY = $this->_getNumber();
            if ($newY === false) 
            {
                if ($first) {
                    printf(
                        "*** Failure reading Y coordinate for '%s' from: '%s' - SKIPPING!!\n",
                        'horizontalLine',
                        $newY
                    );
                }
                return;
            }

            $first = false;

            if ($isRelative) 
            {
                $newY += $this->_lastPoint->y;
            }

            $newPoint = Point::fromValues($this->_lastPoint->x, $newY);

            $this->_actionFromPoint('lineTo', $newPoint);

            $this->_lastPoint->setFromPoint($newPoint);
        }
    }

    private function _horizontalLine($isRelative) 
    {
        $first = true;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $newX = $this->_getNumber();
            if ($newX === false) 
            {
                if ($first) {
                    printf(
                        "*** Failure reading X coordinate for '%s' from: '%s' - SKIPPING!!\n",
                        'horizontalLine',
                        $newX
                    );
                }
                return;
            }

            $first = false;

            if ($isRelative) 
            {
                $newX += $this->_lastPoint->x;
            }

            $newPoint = Point::fromValues($newX, $this->_lastPoint->y);

            $this->_actionFromPoint('lineTo', $newPoint);

            $this->_lastPoint->setFromPoint($newPoint);
        }
    }

    private function _actionFromPoint($aAction, $aPoint) 
    {
        # If next action is a Bézier curve, don´t use $this->_lastControlPoint.
        $this->_lastControlPointType = SVGToTDXSymbolConverter::CONTROL_POINT_TYPE_NONE;

        $converted = $this->_getConvertedPoint($aPoint);

        $this->_tdx .= sprintf(
            "%s%s %s\n", 
            self::INDENT, 
            $aAction,
            $this->_pointToString($converted)
        );
    }

    private function _lineTo($isRelative) 
    {
        $this->_multiplePoint($isRelative, 'lineTo');
    }

    private function _moveTo($isRelative) 
    {
        $this->_multiplePoint($isRelative, 'moveTo');
    }

    private function _consumeNumbers() 
    {
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            if ($this->_getNumber() === false) 
            {
                break;
            }
        }
    }

    private function _setFirstPoint(Point $aPoint) 
    {
        $this->_firstPoint->setFromPoint($aPoint);
        $this->_firstPointSet = true;
    }

    private function _getRestPathData() 
    {
        return substr($this->_pathData, $this->_pathDataIndex);
    }

    private function _getNumber() 
    {
        $newChar = substr($this->_pathData, $this->_pathDataIndex, 1);
        while (($newChar === ' ') ||
            ($newChar === ',')) 
        {
            $this->_pathDataIndex++;
            if ($this->_pathDataIndex < strlen($this->_pathData))
            {
                $newChar = substr($this->_pathData, $this->_pathDataIndex, 1);
            }
            else 
            {
                break;
            }
        }

        $newNumber  = '';
        $first = true;
        $firstDot = true;
        while (is_numeric($newChar) ||
                ($first && 
                    (($newChar == '-') ||
                        ($newChar == '.')
                    )
                ) ||
                ($firstDot && ($newChar == '.'))
            ) 
        {
            if ($first && ($newChar == '.')) 
            {
                $firstDot = false;
                $newNumber = '0.';
            }
            else 
            {
                if ($newChar == '.')
                {
                    if (!$firstDot) {
                        break;
                    }
                    $firstDot = false;
                }
                $newNumber .= $newChar;
            }
            $this->_pathDataIndex++;
            $first = false;
            if ($this->_pathDataIndex < strlen($this->_pathData)) 
            {
                $newChar = substr($this->_pathData, $this->_pathDataIndex, 1);
            }
            else 
            {
                break;
            }
        }
        if (is_numeric($newNumber)) 
        {
            return (float) $newNumber;
        }
        else 
        {
            return false;
        }
    }

    private function _parsePoint($isRelative, $aContext)
    {
        $newX = $this->_getNumber();
        if ($newX === false) 
        {
            return false;
        }
        $newY = $this->_getNumber();
        if ($newY === false) 
        {
            printf(
                "*** Failure reading Y coordinate '%s' from: '%s' - SKIPPING!!\n",
                $aContext,
                $this->_getRestPathData()
            );
            return false;
        }

        if ($isRelative) 
        {
            $newX += $this->_lastPoint->x;
            $newY += $this->_lastPoint->y;
        }
        $newPoint = Point::fromValues($newX, $newY);

        if (!$this->_firstPointSet) 
        {
            $this->_setFirstPoint($newPoint);
        }
        
        return $newPoint;
    }

    private function _multiplePoint($isRelative, $aAction) 
    {
        $first = true;
        while ($this->_pathDataIndex < strlen($this->_pathData)) 
        {
            $newPoint = $this->_parsePoint($isRelative, $aAction);
            if ($newPoint === false) 
            {
                if ($first) 
                {
                    printf(
                        "*** Failure reading X coordinate '%s' from: '%s' - SKIPPING!!\n",
                        $aAction,
                        $this->_getRestPathData()
                    );
                }
                return;
            }

            $this->_actionFromPoint($aAction, $newPoint);

            $this->_lastPoint->setFromPoint($newPoint);

            if ($first) 
            {
                $first = false;
                # With multiple 'moveTos' in the same command, the moves following the first 
                # should be treated as 'lineTos'.
                $aAction = 'lineTo';
            }
        }
    }

    private function _getConvertedLength($aSize)
    {
        $newSize = ($this->_aX * $aSize);

        return $newSize;
    }

    private function _getConvertedPoint(Point $aPoint) 
    {
        $newX = ($this->_aX * $aPoint->x) + $this->_bX;
        if ($newX < $this->_tdxLeft)
        {
            printf(
                "*** X coordinate ('%f') converted from ('%f') smaller than minimum ('%f'): capping\n",
                $newX,
                $aPoint->x,
                $this->_tdxLeft
            );
            $newX = $this->_tdxLeft;
        }
        elseif ($newX > $this->_tdxRight)
        {
            printf(
                "*** X coordinate ('%f') converted from ('%f') bigger than maximum ('%f'): capping\n",
                $newX,
                $aPoint->x,
                $this->_tdxRight
            );
            $newX = $this->_tdxRight;
        }
        $newY = ($this->_aY * $aPoint->y) + $this->_bY;
        if ($newY < $this->_tdxTop)
        {
            printf(
                "*** Y coordinate ('%f') converted from ('%f') smaller than minimum ('%f'): capping\n",
                $newY,
                $aPoint->y,
                $this->_tdxTop
            );
            $newY = $this->_tdxTop;
        }
        elseif ($newY > $this->_tdxBottom)
        {
            printf(
                "*** Y coordinate ('%f') converted from ('%f') bigger than maximum ('%f'): capping\n",
                $newY,
                $aPoint->y,
                $this->_tdxBottom
            );
            $newY = $this->_tdxBottom;
        }
        return Point::fromValues($newX, $newY);
    }

    public function setTDXLimits($aTop, $aBottom, $aLeft, $aRight) 
    {
        $_tdxTop = (float) $aTop;
        $_tdxBottom = (float) $aBottom;
        $_tdxLeft = (float) $aLeft;
        $_tdxRight = (float) $aRight;

        $this->_calculateFactors();
    }
}


//----------------------------------------------------------------------
//
// SVG arcs (from paths) to cubic bézier curves convertion functions.
//
// Based on https://www.npmjs.com/package/svg-arc-to-cubic-bezier
//
//----------------------------------------------------------------------
const TAU = M_PI * 2;
const NEAR_ZERO_FLOAT_EPSILON = PHP_FLOAT_EPSILON / 10;

//
// Sensible float comparison
//
// Based on https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
//
function almostEqual(
    $a,
    $b,
    $maxDiffNearZero = NEAR_ZERO_FLOAT_EPSILON,
    $maxDiffRel = PHP_FLOAT_EPSILON)
{
    // Check if the numbers are really close -- needed
    // when comparing numbers near zero.
     $diff = abs($a - $b);
     if ($diff <= $maxDiffNearZero)
     {
        return true;
     }

     $absA = abs($a);
     $absB = abs ($b);
     $largest = ($absB > $absA) ? $absB : $absA;
     if ($diff <= ($largest * $maxDiffRel))
     {
        return true;
     }
     return false;
}

function mapToEllipse($point, $rx, $ry, $cosphi, $sinphi, $centerx, $centery) 
{
    $x = $point['x'] * $rx;
    $y = $point['y'] * $ry;

    $xp = $cosphi * $x - $sinphi * $y;
    $yp = $sinphi * $x + $cosphi * $y;

    return [
        'x' => $xp + $centerx,
        'y' => $yp + $centery
    ];
}

function approxUnitArc($ang1, $ang2) 
{
    // If 90 degree circular arc, use a constant
    // as derived from http://spencermortensen.com/articles/bezier-circle
    $a = (almostEqual($ang2, 1.5707963267948966)) ? 0.551915024494 : 
        ((almostEqual($ang2, -1.5707963267948966)) ? -0.551915024494 : 
            4 / 3 * tan($ang2 / 4));

    $x1 = cos($ang1);
    $y1 = sin($ang1);
    $x2 = cos($ang1 + $ang2);
    $y2 = sin($ang1 + $ang2);

    return [
        0 => [
            'x' => $x1 - $y1 * $a,
            'y' => $y1 + $x1 * $a,
        ],
        1 => [
            'x' => $x2 + $y2 * $a,
            'y' => $y2 - $x2 * $a,
        ],
        2 => [
            'x' => $x2,
            'y' => $y2,
        ],
    ];
}

function vectorAngle($ux, $uy, $vx, $vy) 
{
    $sign = (($ux * $vy - $uy * $vx) < 0) ? -1 : 1;
    $dot = $ux * $vx + $uy * $vy;

    // rounding errors, e.g. -1.0000000000000002 can screw up this
    if ($dot > 1) {
        $dot = 1;
    }

    if ($dot < -1) {
        $dot = -1;
    }

    return $sign * acos($dot);
}

function getArcCenter(
    $px, 
    $py, 
    $cx, 
    $cy, 
    $rx, 
    $ry, 
    $largeArcFlag, 
    $sweepFlag, 
    $sinphi, 
    $cosphi, 
    $pxp, 
    $pyp) 
{
    $rxsq = $rx * $rx;
    $rysq = $ry * $ry;
    $pxpsq = $pxp * $pxp;
    $pypsq = $pyp * $pyp;

    $radicant = ($rxsq * $rysq) - ($rxsq * $pypsq) - ($rysq * $pxpsq);

    if ($radicant < 0) {
        $radicant = 0;
    }

    $radicant /= ($rxsq * $pypsq) + ($rysq * $pxpsq);
    $radicant = sqrt($radicant) * ($largeArcFlag == $sweepFlag ? -1 : 1);

    $centerxp = $radicant * $rx / $ry * $pyp;
    $centeryp = $radicant * -$ry / $rx * $pxp;

    $centerx = $cosphi * $centerxp - $sinphi * $centeryp + ($px + $cx) / 2;
    $centery = $sinphi * $centerxp + $cosphi * $centeryp + ($py + $cy) / 2;

    $vx1 = ($pxp - $centerxp) / $rx;
    $vy1 = ($pyp - $centeryp) / $ry;
    $vx2 = (-$pxp - $centerxp) / $rx;
    $vy2 = (-$pyp - $centeryp) / $ry;

    $ang1 = vectorAngle(1, 0, $vx1, $vy1);
    $ang2 = vectorAngle($vx1, $vy1, $vx2, $vy2);

    if ($sweepFlag == 0 && $ang2 > 0) {
        $ang2 -= TAU;
    }

    if ($sweepFlag == 1 && $ang2 < 0) {
        $ang2 += TAU;
    }

    return [$centerx, $centery, $ang1, $ang2];
}

function arcToBezier(
        $px,
        $py,
        $cx,
        $cy,
        $rx,
        $ry,
        $xAxisRotation,
        $largeArcFlag,
        $sweepFlag    
    ) 
{
    $curves = [];

    if ($rx == 0 || $ry == 0) {
        return [];
    }

    $sinphi = sin($xAxisRotation * TAU / 360);
    $cosphi = cos($xAxisRotation * TAU / 360);

    $pxp = $cosphi * ($px - $cx) / 2 + $sinphi * ($py - $cy) / 2;
    $pyp = -$sinphi * ($px - $cx) / 2 + $cosphi * ($py - $cy) / 2;

    if (almostEqual($pxp, 0) && almostEqual($pyp, 0)) {
        return [];
    }

    $rx = abs($rx);
    $ry = abs($ry);

    $lambda = (pow($pxp, 2) / pow($rx, 2) + 
        pow($pyp, 2) / pow($ry, 2));

    if ($lambda > 1) {
        $rx *= sqrt($lambda);
        $ry *= sqrt($lambda);
    }

    list($centerx, 
        $centery, 
        $ang1, 
        $ang2) = getArcCenter(
            $px, 
            $py, 
            $cx, 
            $cy, 
            $rx, 
            $ry, 
            $largeArcFlag, 
            $sweepFlag, 
            $sinphi, 
            $cosphi, 
            $pxp, 
            $pyp);

    // If 'ang2' == 90.0000000001, then `ratio` will evaluate to
    // 1.0000000001. This causes `segments` to be greater than one, which is an
    // unnecessary split, and adds extra points to the bezier curve. To alleviate
    // this issue, we round to 1.0 when the ratio is close to 1.0.
    $ratio = abs($ang2) / (TAU / 4);
    if (almostEqual(1, $ratio)) {
        $ratio = 1;
    }

    $segments = max(ceil($ratio), 1);

    $ang2 /= $segments;

    for ($i = 0; $i < $segments; $i++) {
        $curves[] = approxUnitArc($ang1, $ang2);
        $ang1 += $ang2;
    }

    return array_map(function ($curve) use ($rx, $ry, $cosphi, $sinphi, $centerx, $centery) {
        $point1 = mapToEllipse($curve[0], $rx, $ry, $cosphi, $sinphi, $centerx, $centery);
        $point2 = mapToEllipse($curve[1], $rx, $ry, $cosphi, $sinphi, $centerx, $centery);
        $point3 = mapToEllipse($curve[2], $rx, $ry, $cosphi, $sinphi, $centerx, $centery);

        return [
            'x1' => $point1['x'],
            'y1' => $point1['y'],
            'x2' => $point2['x'],
            'y2' => $point2['y'],
            'x' => $point3['x'],
            'y' => $point3['y']
        ];
    }, $curves);
}
//---------------------------------------------------------------------------
//
// End of SVG arcs (from paths) to cubic bézier curves convertion functions.
//
//---------------------------------------------------------------------------


function help()
{
    printf(<<<HELP

Usage: %s SVG_FILE [DECIMAL_PLACES]

    SGV_FILE - svg file to be converted
    DECIMAL_PLACES - (optional) amount of desired decimal places in the output. Default: 1

    * Converts a SVG file to TopoDroidX's symbol drawing language.
    * 
    * It only supports circles and paths. All other element types are silently ignored.
    * 
    * Only the outline of the elements are considered, all other attributes are 
    * silently ignored.
    * 
    * If you used any other shape in the SVG file, convert it to paths before trying 
    * to use this script.
    * 
    * It's possible to set the amount of decimal places to be generated but the default 
    * of 1 seems to be really good.
    * 
    * Usage:
    * 
    * 1. Create you symbol definition in the vector art software of your choice.
    * 
    * 2. Export it as a SVG file.
    * 
    * 3. Convert the file using this script.
    * 
    * 4. Copy the output of this script to the 'path' section inside the TopoDroidX
    *    symbol definition.
    * 
    * 5. Enjoy your new TopoDroid symbol!
    * 
    * Observations:
    * 
    * 1. The viewBox of the SVG file is used to set the scaling factor to the 
    *    (-10,-10) x (10,10) TopoDroids canvas. SVGs viewBox attribute is usually 
    *    set by the paper size or document properties in vector art software. 
    *    Prefer square viewBoxes!
    * 
    * 2. If the viewbox is not a square, the smaller dimension (width or height)
    *    is increased to match the other so shapes are not distorted.
    * 
    * 3. Have fun creating TopoDroid symbols!
    * 
    * 4. Remember to share your new symbols!!


HELP
,
        basename(__FILE__)  
    );
}

if (($argc < 2) || ($argc > 3))
{
    help();
    die();
}
// Load the SVG file
$svgFile = trim($argv[1]);

$decimalPlaces = (($argc > 2) && is_numeric($argv[2])) ? (int) $argv[2] : 1; 

$converter = new SVGToTDXSymbolConverter();
$converter->setDecimalPlaces($decimalPlaces);
$conversion = $converter->convertFile($svgFile);

echo "Convertion result:\n";
echo $conversion . "\n";
