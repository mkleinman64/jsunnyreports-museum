<?php
/**
 * Adds jSunnyreports widget.
 */
class JSunnyreports_Widget extends WP_Widget {

	/**
	 * Register widget with WordPress.
	 */
	function __construct() {
		parent::__construct(
			'jsunnyreports_widget', // Base ID
			esc_html__( 'jSunnyreports actualinfo', 'jsr_domain' ), // Name
			array( 'description' => esc_html__( 'Displays actualinfo.json', 'jsr_domain' ), ) // Args
		);
	}

	/**
	 * Front-end display of widget.
	 *
	 * @see WP_Widget::widget()
	 *
	 * @param array $args     Widget arguments.
	 * @param array $instance Saved values from database.
	 */
	public function widget( $args, $instance ) {
		echo $args['before_widget'];
		if ( ! empty( $instance['title'] ) ) {
			echo $args['before_title'] . apply_filters( 'widget_title', $instance['title'] ) . $args['after_title'];
        }
        
        // widget content output
        if ( ! empty( $instance['jsr_path'] ) ) {
            $url = $instance['jsr_path'];
            if ( strcmp( substr( $url, -1 ), '/' ) !== 0 ) {
                $url = $url . '/';
            }
            $url = $url . 'json/actualinfo.json';
            $request = wp_remote_get( $url );

            if( is_wp_error( $request ) ) {
                return false; // Bail early
            }  
            
            $body = wp_remote_retrieve_body( $request );
            $jsr_data = json_decode( $body );

            if ( !empty( $jsr_data ) ) {

                $actualW = round($jsr_data->Actual);
                $peakW   = round($jsr_data->Peak);
                $today   = round($jsr_data->Today,1);

                echo '<h4>Current</h4>';
                echo '<p>';
                echo '<ul>';
                echo '<li>Actual production: <b>'. $actualW .'W</b></li>';
                echo '<li>Todays max: <b>' . $peakW . 'W</b></li>';
                echo '<li>Yield today: <b>' . $today . 'kWh</b></li>';
                echo '</ul>';
                echo '</p>';

                echo '<h4>Inverters</h4>';
                echo '<p>';
                echo '<ul>';

                foreach ( $jsr_data->inverteryield as $inverter ) {
                    $now = round( $inverter->now );
                    echo '<li>' . $inverter->invertername. ': <b>'. $now .'W</b></li>';
                }

                
                echo '</ul>';
                echo '</p>';


            }

        }


		echo $args['after_widget'];
	}

	/**
	 * Back-end widget form.
	 *
	 * @see WP_Widget::form()
	 *
	 * @param array $instance Previously saved values from database.
	 */
	public function form( $instance ) {
        $title = ! empty( $instance['title'] ) ? $instance['title'] : esc_html__( 'PV output', 'jsr_domain' );
        $jsr_path = ! empty( $instance['jsr_path'] ) ? $instance['jsr_path'] : '';
        
		?>

		<p>
		    <label for="<?php echo esc_attr( $this->get_field_id( 'title' ) ); ?>"><?php esc_attr_e( 'Title:', 'jsr_domain' ); ?></label> 
		    <input 
                class="widefat" id="<?php echo esc_attr( $this->get_field_id( 'title' ) ); ?>" 
                name="<?php echo esc_attr( $this->get_field_name( 'title' ) ); ?>" 
                type="text" 
                value="<?php echo esc_attr( $title ); ?>">
		</p>

		<p>
		    <label for="<?php echo esc_attr( $this->get_field_id( 'jsr_path' ) ); ?>"><?php esc_attr_e( 'jSunnyreports path:', 'jsr_domain' ); ?></label> 
		    <input 
                class="widefat" id="<?php echo esc_attr( $this->get_field_id( 'jsr_path' ) ); ?>" 
                name="<?php echo esc_attr( $this->get_field_name( 'jsr_path' ) ); ?>" 
                type="text" 
                value="<?php echo $jsr_path; ?>">
		</p>

     
		<?php 
	}

	/**
	 * Sanitize widget form values as they are saved.
	 *
	 * @see WP_Widget::update()
	 *
	 * @param array $new_instance Values just sent to be saved.
	 * @param array $old_instance Previously saved values from database.
	 *
	 * @return array Updated safe values to be saved.
	 */
	public function update( $new_instance, $old_instance ) {
		$instance = array();
		$instance['title'] = ( ! empty( $new_instance['title'] ) ) ? sanitize_text_field( $new_instance['title'] ) : '';
		$instance['jsr_path'] = ( ! empty( $new_instance['jsr_path'] ) ) ? sanitize_text_field( $new_instance['jsr_path'] ) : '';

		return $instance;
	}

} // class JSunnyreports_Widget

?>