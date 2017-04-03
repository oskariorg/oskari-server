package fi.nls.oskari.control.statistics.xml;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import fi.nls.test.util.ResourceHelper;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

@RunWith(PowerMockRunner.class)
public class WfsXmlParserTest {
    private static String testResponseKunta = ResourceHelper.readStringResource("geoserver-GetFeature.xml",
            WfsXmlParserTest.class);
    private static String testResponseSeutukunta = ResourceHelper.readStringResource("geoserver-seutukunta.xml",
            WfsXmlParserTest.class);

    @Test
    public void testParseKuntaRegions() throws IOException {
        InputStream is = new ByteArrayInputStream(testResponseKunta.getBytes("UTF-8"));
        List<Region> result = WfsXmlParser.parse(is, "kuntakoodi", "kuntanimi");
        assertEquals("[[005, Alajärvi], [009, Alavieska], [010, Alavus], [016, Asikkala], [018, Askola], [019, Aura], [043, Eckerö], [046, Enonkoski], [047, Enontekiö], [049, Espoo], [035, Brändö], [050, Eura], [103, Humppila], [051, Eurajoki], [052, Evijärvi], [099, Honkajoki], [060, Finström], [061, Forssa], [069, Haapajärvi], [071, Haapavesi], [072, Hailuoto], [074, Halsua], [105, Hyrynsalmi], [106, Hyvinkää], [108, Hämeenkyrö], [256, Kinnula], [062, Föglö], [065, Geta], [075, Hamina], [076, Hammarland], [077, Hankasalmi], [092, Vantaa], [097, Hirvensalmi], [098, Hollola], [102, Huittinen], [078, Hanko], [079, Harjavalta], [081, Hartola], [082, Hattula], [086, Hausjärvi], [090, Heinävesi], [091, Helsinki], [109, Hämeenlinna], [139, Ii], [140, Iisalmi], [142, Iitti], [143, Ikaalinen], [145, Ilmajoki], [146, Ilomantsi], [148, Inari], [171, Joroinen], [172, Joutsa], [214, Kankaanpää], [149, Inkoo], [151, Isojoki], [152, Isokyrö], [153, Imatra], [164, Jalasjärvi], [165, Janakkala], [204, Kaavi], [205, Kajaani], [167, Joensuu], [169, Jokioinen], [170, Jomala], [174, Juankoski], [176, Juuka], [177, Juupajoki], [178, Juva], [179, Jyväskylä], [181, Jämijärvi], [182, Jämsä], [186, Järvenpää], [202, Kaarina], [208, Kalajoki], [211, Kangasala], [213, Kangasniemi], [250, Kihniö], [216, Kannonkoski], [217, Kannus], [218, Karijoki], [224, Karkkila], [226, Karstula], [230, Karvia], [231, Kaskinen], [232, Kauhajoki], [233, Kauhava], [235, Kauniainen], [236, Kaustinen], [239, Keitele], [240, Kemi], [241, Keminmaa], [244, Kempele], [245, Kerava], [249, Keuruu], [257, Kirkkonummi], [260, Kitee], [261, Kittilä], [263, Kiuruvesi], [265, Kivijärvi], [271, Kokemäki], [280, Korsnäs], [283, Hämeenkoski], [284, Koski Tl], [687, Rautavaara], [689, Rautjärvi], [691, Reisjärvi], [272, Kokkola], [273, Kolari], [275, Konnevesi], [276, Kontiolahti], [312, Kyyjärvi], [316, Kärkölä], [317, Kärsämäki], [505, Mäntsälä], [476, Maaninka], [831, Taipalsaari], [285, Kotka], [286, Kouvola], [287, Kristiinankaupunki], [288, Kruunupyy], [290, Kuhmo], [291, Kuhmoinen], [295, Kumlinge], [297, Kuopio], [300, Kuortane], [301, Kurikka], [538, Nousiainen], [541, Nurmes], [543, Nurmijärvi], [304, Kustavi], [305, Kuusamo], [309, Outokumpu], [592, Petäjävesi], [593, Pieksämäki], [694, Riihimäki], [318, Kökar], [319, Köyliö], [320, Kemijärvi], [398, Lahti], [399, Laihia], [400, Laitila], [402, Lapinlahti], [403, Lappajärvi], [405, Lappeenranta], [407, Lapinjärvi], [408, Lapua], [410, Laukaa], [413, Lavia], [416, Lemi], [441, Luumäki], [595, Pielavesi], [697, Ristijärvi], [442, Luvia], [475, Maalahti], [417, Lemland], [418, Lempäälä], [420, Leppävirta], [421, Lestijärvi], [422, Lieksa], [423, Lieto], [425, Liminka], [426, Liperi], [430, Loimaa], [433, Loppi], [434, Loviisa], [435, Luhanka], [436, Lumijoki], [438, Lumparland], [440, Luoto], [478, Maarianhamina], [480, Marttila], [481, Masku], [483, Merijärvi], [598, Pietarsaari], [599, Pedersören kunta], [601, Pihtipudas], [484, Merikarvia], [489, Miehikkälä], [491, Mikkeli], [494, Muhos], [495, Multia], [498, Muonio], [499, Mustasaari], [500, Muurame], [503, Mynämäki], [504, Myrskylä], [507, Mäntyharju], [529, Naantali], [531, Nakkila], [532, Nastola], [535, Nivala], [536, Nokia], [545, Närpiö], [560, Orimattila], [561, Oripää], [562, Orivesi], [563, Oulainen], [604, Pirkkala], [607, Polvijärvi], [608, Pomarkku], [684, Rauma], [564, Oulu], [576, Padasjoki], [577, Paimio], [578, Paltamo], [580, Parikkala], [581, Parkano], [583, Pelkosenniemi], [584, Perho], [588, Pertunmaa], [609, Pori], [611, Pornainen], [614, Posio], [615, Pudasjärvi], [616, Pukkila], [619, Punkalaidun], [620, Puolanka], [623, Puumala], [681, Rantasalmi], [683, Ranua], [686, Rautalampi], [624, Pyhtää], [625, Pyhäjoki], [626, Pyhäjärvi], [630, Pyhäntä], [631, Pyhäranta], [635, Pälkäne], [636, Pöytyä], [678, Raahe], [680, Raisio], [698, Rovaniemi], [700, Ruokolahti], [702, Ruovesi], [704, Rusko], [707, Rääkkylä], [729, Saarijärvi], [732, Salla], [734, Salo], [736, Saltvik], [738, Sauvo], [739, Savitaipale], [740, Savonlinna], [742, Savukoski], [743, Seinäjoki], [746, Sievi], [747, Siikainen], [748, Siikajoki], [749, Siilinjärvi], [751, Simo], [753, Sipoo], [755, Siuntio], [758, Sodankylä], [759, Soini], [761, Somero], [762, Sonkajärvi], [765, Sotkamo], [766, Sottunga], [768, Sulkava], [771, Sund], [777, Suomussalmi], [778, Suonenjoki], [781, Sysmä], [783, Säkylä], [785, Vaala], [832, Taivalkoski], [833, Taivassalo], [834, Tammela], [837, Tampere], [838, Tarvasjoki], [844, Tervo], [845, Tervola], [846, Teuva], [848, Tohmajärvi], [849, Toholampi], [850, Toivakka], [851, Tornio], [853, Turku], [854, Pello], [857, Tuusniemi], [858, Tuusula], [859, Tyrnävä], [886, Ulvila], [887, Urjala], [889, Utajärvi], [992, Äänekoski], [111, Heinola], [890, Utsjoki], [892, Uurainen], [893, Uusikaarlepyy], [905, Vaasa], [908, Valkeakoski], [911, Valtimo], [915, Varkaus], [918, Vehmaa], [921, Vesanto], [922, Vesilahti], [924, Veteli], [895, Uusikaupunki], [925, Vieremä], [927, Vihti], [931, Viitasaari], [934, Vimpeli], [935, Virolahti], [936, Virrat], [444, Lohja], [638, Porvoo], [020, Akaa], [322, Kemiönsaari], [710, Raasepori], [508, Mänttä-Vilppula], [790, Sastamala], [445, Parainen], [791, Siikalatva], [941, Vårdö], [976, Ylitornio], [977, Ylivieska], [980, Ylöjärvi], [981, Ypäjä], [989, Ähtäri], [946, Vöyri]]",
                result.toString());
    }

    @Test
    public void testParseSeutukuntaRegions() throws IOException {
        InputStream is = new ByteArrayInputStream(testResponseSeutukunta.getBytes("UTF-8"));
        List<Region> result = WfsXmlParser.parse(is, "seutukuntanro", "seutukunta");
        assertEquals("[[082, Kotka-Haminan], [124, Keski-Karjalan], [101, Mikkelin], [181, Kehys-Kainuun], [211, Mariehamns stad], [024, Vakka-Suomen], [043, Porin], [193, Torniolaakson], [213, Ålands skärgård], [091, Lappeenrannan], [133, Keuruun], [093, Imatran], [135, Äänekosken], [153, Sydösterbottens kustregion], [112, Kuopion], [174, Raahen], [044, Pohjois-Satakunnan], [194, Itä-Lapin], [191, Rovaniemen], [196, Tunturi-Lapin], [105, Pieksämäen], [212, Ålands landsbygd], [113, Koillis-Savon], [053, Forssan], [141, Suupohjan], [161, Kaustisen], [125, Pielisen-Karjalan], [071, Lahden], [197, Pohjois-Lapin], [063, Etelä-Pirkanmaan], [041, Rauman], [144, Kuusiokuntien], [064, Tampereen], [134, Jämsän], [115, Sisä-Savon], [052, Riihimäen], [192, Kemi-Tornion], [154, Jakobstadsregionen], [016, Loviisan], [146, Järviseudun], [011, Helsingin], [015, Porvoon], [069, Ylä-Pirkanmaan], [178, Koillismaan], [177, Ylivieskan], [103, Savonlinnan], [138, Saarijärven-Viitasaaren], [122, Joensuun], [025, Loimaan], [162, Kokkolan], [114, Varkauden], [182, Kajaanin], [023, Turun], [014, Raaseporin], [175, Haapaveden-Siikalatvan], [061, Luoteis-Pirkanmaan], [173, Oulunkaaren], [131, Jyväskylän], [081, Kouvolan], [142, Seinäjoen], [132, Joutsan], [151, Kyrönmaa], [111, Ylä-Savon], [021, Åboland-Turunmaan], [171, Oulun], [176, Nivala-Haapajärven], [022, Salon], [068, Lounais-Pirkanmaan], [051, Hämeenlinnan], [152, Vaasan]]",
                result.toString());
    }

    @Test(expected = IOException.class)
    public void testParseSeutukuntaRegionsWithWrongProps() throws IOException {
        InputStream is = new ByteArrayInputStream(testResponseSeutukunta.getBytes("UTF-8"));
        List<Region> result = WfsXmlParser.parse(is, "asdf", "qwer");
        fail("Error not sent with faulty region parsing");
    }
}
