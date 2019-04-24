/* EditUserLayer has updated dasharray values with Oskari style values. Convert Oskari
 * style values to original values to make SLD styling work properly'
 * */
update user_layer_style set border_dasharray = '' where border_dasharray = 'solid';
update user_layer_style set border_dasharray = '5 2' where border_dasharray = 'dash';
update user_layer_style set stroke_dasharray = '' where stroke_dasharray = 'solid';
update user_layer_style set stroke_dasharray = '5 2' where stroke_dasharray = 'dash';