/* Convert line styles from double to solid (=empty string)
 * 
 * */
update categories set border_dasharray = '' where border_dasharray = 'D';
update categories set stroke_dasharray = '' where stroke_dasharray = 'D';
