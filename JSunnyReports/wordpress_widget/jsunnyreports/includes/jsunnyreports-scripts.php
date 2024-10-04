<?php

// Scripts
function jsr_add_scripts() {
    // Add main CSS
    wp_enqueue_style('jsr-main-style', plugins_url() . '/jsunnyreports/css/style.css' );
    // Add main JS
    wp_enqueue_script('jsr-main-script', plugins_url() . '/jsunnyreports/js/main.css' );
}

add_action( 'wp_enqueue_scripts', 'jsr_add_scripts' );

?>