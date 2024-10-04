<?php
/*
Plugin Name: jSunnyreports widget
Plugin URI: http://www.jsunnyreports.com
Description: Displays actualinfo.json as a widget in your wordpress site.
Version: 0.0.1
Author: Martin Kleinman
Author URI: http://www.jsunnyreports.com
*/

// base check, security, if plugin is loaded directly.
if(!defined('ABSPATH'))  {
    exit;
}

// load scripts.
require_once( plugin_dir_path(__FILE__) . '/includes/jsunnyreports-scripts.php' );

// load main widget class
require_once( plugin_dir_path(__FILE__) . '/includes/jsunnyreports-class.php' );

// register widget
function register_jsunnyreports_widget() {
    register_widget('JSunnyreports_Widget');
}

// Hook in
add_action( 'widgets_init', 'register_jsunnyreports_widget' );
?>