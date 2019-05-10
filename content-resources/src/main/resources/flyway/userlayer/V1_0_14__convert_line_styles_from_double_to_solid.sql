/* Convert line styles from double to solid (=empty string)
 * 
 * */
update user_layer_style set border_dasharray = '' where border_dasharray = 'D';
update user_layer_style set stroke_dasharray = '' where stroke_dasharray = 'D';
