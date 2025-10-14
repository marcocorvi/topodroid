#!/usr/bin/perl
#

open(OUT, ">tot.htm") || die "Cannot open output file\n";
print OUT "<html><body>\n";

@html = (
manual00,
page_quick,
manual01,
manual02,
manual03,
manual04,
manual05,
manual06,
manual07,
manual08,
manual09,
manual10,
manual11,
manual12,
manual13,
manual14,
manual15,
manual16,
# manual17, # moved at the end
# manual18,
page_setup,
page_subdirs,
page_perms,
# page_dialog_r,
page_database,
page_aux,
page_colors,
page_help_info,
page_settings,
page_pref_search,
page_pt_cmap,
page_geek,
page_cwd,
page_keypad,
page_language,
# page_cosurvey,
page_device_name,
page_device_select,
page_device_add,
page_bluetooth,
page_bt_alias,
page_trouble,
page_cavway,
page_cavway_info,
page_cavway_memory,
page_distox,
page_a3_info,
page_x310_info,
page_xble_info,
page_calib_howto,
page_calib_dialog,
page_gm,
page_gm_search,
page_groups,
page_calib_distrib,
page_calib_coeffs,
page_validation,
page_calib_import,
page_packets,
page_memory,
page_firmware,
page_bric,
page_bric_calib,
page_bric_info,
page_bric_memory,
page_sap,
page_new_survey,
page_team,
page_import,
page_calib_check,
page_fixed_list,
page_fixed_add,
page_fixed_gps,
page_fixed_import,
page_fixed,
page_survey_notes,
page_survey_rename,
page_survey_split_move,
page_stat1,
page_download,
page_man_data,
page_survey_calib,
page_station_policy,
page_current,
page_saved_station,
page_stations_map,
page_station_photo,
page_search,
page_azimuth,
page_accuracy,
page_displaymode,
page_shot_edit1,
page_multishot,
page_recompute_dialog,
page_reduction_errors,
page_undelete,
page_photo_list,
page_photo_dialog,
page_photo_edit,
page_photo_view,
page_geocode,
page_audio,
page_audio_list,
page_sensors_list,
page_sensors,
page_sensors_edit,
page_trilateration,
page_new_plot,
page_dangling,
page_plot_list,
page_projection,
page_drawing_refs,
page_scrap_outline,
page_tools,
page_tools_list,
page_symbol_point,
page_symbol_line,
page_symbol_area,
page_symbol_reload,
page_backup,
page_undo,
page_item_edit,
page_sketch_station,
page_therion_stations,
page_drawing_shot,
page_drawing_point,
page_label_dialog,
page_section_point,
page_photo_edit_dialog,
page_line_dialog,
page_area_dialog,
# page_walls,
page_stat2,
page_shift,
page_plot_rename,
page_plot_scraps,
page_plot_merge,
page_plot_zoomfit,
page_graph_paper_scale,
page_plot_export,
page_overview_refs,
page_finalmap,
page_tdm_config,
page_tdm_config_dialog,
page_tdm_info,
page_tdm_view,
page_tdm_equate_new,
page_tdm_equates,
page_tdm_sources,
page_tdm_export,
page_3d_view,
page_3d_proj,
page_3d_info,
page_3d_legs,
page_3d_station,
page_3d_measure,
page_3d_surveys,
page_3d_survey,
page_3d_surface,
page_3d_walls,
page_3d_rose,
page_3d_ico,
page_highlight,
# page_3d_sketches,
# page_3d_sketch,
# page_3d_fractal,
page_3d_export,
# page_sketch_mode,
manual17, # moved at the end
);

@month = qw( Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec );
($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) = localtime();
$year += 1900;

open( DAT, "../AndroidManifest.xml" );
while ( $line = <DAT> ) {
  last if ( $line =~ /android:versionName/ );
}
$line =~ s/^\s*android:versionName="//;
$line =~ s/"\s*$//;
$version = $line;
close DAT;

foreach $i (@html) {
  open( DAT, "$i.htm" );
  while ( $line = <DAT> ) {
    if ( $line =~ /<!-- NOW -->/ ) {
      print OUT "Version $version - $mday  $month[$mon], $year <br>";
      next;
    }
    if ( $line =~ /<html>/ ) {
      print OUT "<a name=\"$i\" />\n";
    } elsif ( $line =~ /<\/html>/ ) {
      print OUT "<br clear=\"all\">\n";
    } elsif ( $line =~ /<!--/ ) {
      if ( not $line =~ /-->/ ) {
        while ( $line = <DAT> ) {
          last if ( $line =~ /-->/ );
        }
      }
    } elsif ( $line =~ / &gt;<\/a>/ ) {
      print OUT "<hr>\n";
    } elsif ($line =~ /htm">&lt; / ) {
      # skip
    } else {
      $line =~ s/href="/href="#/g;
      $line =~ s/\.htm//g;
      print OUT $line;
    }
  }
  close DAT;
}
