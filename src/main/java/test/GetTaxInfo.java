package test;

import assessment.IndexAssessmentTask;
import taxonomy.NCBITaxonomy;
import taxonomy.TAXONOMIC_RANK;

/**
 * Created by joachim on 24.09.19.
 */
public class GetTaxInfo {
    public static void main(String[] args) {
        NCBITaxonomy taxonomy = new NCBITaxonomy("nodes.dmp", "names.dmp");

        int[] taxids = new int[] {1895743,112987,204429,2,282132,83552,92713,1414722,2052048,1963360,1444711,1314958,1437425,809,340071,71667,186802,483423,83551,286,204428,1478175,1871323,1444712,1478174,1353976,51291,389348,83561,1704234,1871324,1485,810,1236,362787,1239,2081524,562,1973936,1783272,1973935,434307,838,816,1797605,171549,1895742,1301,813,1279,1871322,1785087,1313,294,112235,83555,572511,159254,1716,2053303,186803,412447,1797603,1353977,200643,31979,287,1811695,1730,1705700,79206,85991,83558,186807,53408,1263,1307,1493,1967783,2170265,186928,2030927,28901,1704235,1280,316,1496,303,1898203,1265,1112244,1912599,541000,59620,1725,29355,2078660,655338,264201,989,216851,59510,561,1560217,817,99656,1806891,2056847,1898207,91061,1760,2043169,1985876,1335,106588,839,2013,1278232,461876,1628158,1912897,1314,265470,1491,477666,1531962,1776382,1513,1658111,269666,1165092,1405396,289370,65551,1777880,836,59201,253314,420412,317,1499,1235799,830,59619,1473580,270,1262932,45254,165185,1720194,831,51197,543,1872471,1112230,428992,2382,398199,1640674,28131,1304284,1449126,1792311,35825,237609,1410621,2009038,84032,1477105,1408304,1897044,198616,2070620,2292046,180164,889453,853,312306,1520828,1811693,1262881,866673,204434,474950,1650662,1264,83559,385682,29357,2070636,225345,39482,1667357,300,982,39488,81475,2174842,341220,375288,1677857,84022,1283,292800,1970189,1151391,1262734,1688776,77095,397288,1329262,2292274,2292236,2021971,1965570,1797683,114527,587753,1855325,1967782,671267,1720294,46506,33028,1262924,251699,937254,1506994,1566248,1898651,1235800,714197,940615,310297,1339253,94868,2024216,1487923,1307839,702438,256466,1301100,504270,1917873,83556,575200,1263067,171550,442430,270498,2053618,1557,2292054,97253,294710,186804,765953,53437,1262865,1262849,1262942,2320083,1761012,1179778,94869,1262814,165186,47246,470565,1892843,91623,136849,84030,649756,1911556,1392836,913865,1761781,1958780,46503,1504,1529,574962,265975,1622073,2292066,482827,598660,172733,51515,1306993,1487582,1766203,2723,2734,1121358,626931,37658,1262693,1262840,1501392,2383,2219695,1897042,283734,1717717,1718,1280667,1391647,52689,233055,44754,79209,1553,1410630,940614,2292348,2038470,76759,2006847,1470354,1761889,328812,1308,1262944,43263,254161,1884657,1346,558415,346377,870903,940557,2292912,458252,54294,2020949,1731,1346286,2485170,28033,1168034,1502,208479,2093368,33954,1562,1501226,543314,1158294,1797682,679937,2044573,1849176,653930,1285191,1797601,2293838,1780379,56112,1888195,397291,1536651,1573806,1520817,99594,1783257,1673726,1147123,1302,243161,169679,411477,1776045,392734,1965603,501010,165179,1702250,392733,1793966,86332,1884669,66219,1745713,1303,116090,1410626,227319,39496,454155,1897012,218205,42447,1898204,137658,39491,47877,351091,1703353,908809,93975,1776391,85569,1855342,53422,2045015,1288,2082193,1941478,73919,29362,2024222,2302962,501571,626933,189330,1987504,341036,157782,264641,1986155,2079234,1137848,408753,487184,36841,467085,225762,841,119641,1296,1527,390884,915471,244127,1686289,1465754,1262913,1889883,166485,1121298,1408321,36842,426128,1897011,1111069,1262934,1494,1972642,84698,2071715,1469948,142877,33042,1639035,2302949,2320089,522492,395922,453960,28111,1200751,1898205,796937,1985301,1739529,1035308,1965580,1429083,139043,1470356,86170,1200721,863,1505,214904,1858795,1163671,33952,1262838,1262953,1410622,1115822,1871018,1981685,1749078,53633,1852383,332951,1193532,161890,52022,1895718,1703352,1478221,56957,1859290,278961,131567,312168,46127,177971,2292231,68298,1297424,1449080,1897043,29344,1262742,33951,1262808,2282740,1537566,399320,1198456,2382159,1776384,191391,310300,1903953,77298,204475,1458463,1852370,1263040,1853231,35755,1736407,483215,36845,53409,37636,1262797,1492,349931,100174,645887,1497953,1965562,556548,53412,1348235,2009003,239759,187137,1514668,1797606,1986604,208962,53406,1245526,999898,46206,2184575,2135584,759821,1231000,1776047,1383067,47882,1424294,195950,1895899,28112,40518,1121883,1347366,474952,306,246432,2056865,156973,1561,1564,136845,370438,2724,76632,1715187,1805478,1400053,40575,237576,286730,426129,29384,1262965,169435,157784,1137268,1403537,191373,1408319,171551,712437,1945634,277,487732,1896989,160404,50340,1262810,1262815,1294,1501,60133,1965019,863370,515393,171552,2293840,2040291,1450648,29349,1930532,1262889,132476,2292368,1449050,1408322,1965575,1499686,1200747,1262802,1262821,1565,1410628,1727,2173034,154046,1849822,2741,1239788,133925,35700,1776758,1658108,1720316,1978337,86331,1434072,234828,1533,1410624,104087,2008913,1321783,1903506,1633201,580596,1121301,2305133,28034,1678841,1965631,102134,36853,148,57743,1216932,70411,1655638,29524,2093891,2053305,1520827,2282741,1811692,1305,1262928,1262945,652706,402877,521720,292632,39485,1739421,1497955,93466,2293125,1965538,1797680,188786,930166,29367,1708783,29435,1397668,1434837,1262847,46677,2320081,264463,715222,1739389,1280668,936595,1280686,109327,43997,1506553,1965555,1965576,1797678,36746,36834,237610,286802,2093370,296,1484053,1262927,1520,1534,136841,1734395,231233,329936,43305,2447885,1841855,588605,2018667,990721,474960,36745,397290,2015553,29372,393921,1639118,58138,877420,1573805,185300,2044936,1262837,91624,2382125,658087,273376,2067550,1895719,28037,155085,122355,1965569,1117708,1232438,1764958,655355,319475,36874,946234,2045012,1131707,1520826,1262820,1304,1311,1385,1510,1185412,100134,1263547,313439,215200,288965,186831,1202667,191027,72304,1280685,760568,1325933,314319,1805470,2293022,2305463,28115,1871336,1797677,1564487,36849,217158,2052179,65741,2162893,69894,512314,1888891,29322,37659,837,979970,1262826,1326,1262894,1263054,1519,1721,206681,2009042,161879,321662,162156,141693,207244,43770,109329,39779,1833852,35701,879566,35830,658457,1658009,670905,236753,44258,28116,1965549,1969835,1048380,884684,1581088,1499683,29341,1409788,820,1541063,1852363,2479048,70775,291995,1520812,2290935,238834,1262829,206096,529704,42322,1135990,1542,2086585,702115,42858,84024,1910938,88431,2422,682400,1280672,35517,1235790,1608583,314318,1940526,1805471,208226,52704,28130,1007099,1965590,1629716,445972,1605376,65403,339862,360422,315405,1531961,1515613,53343,2068654,553151,90370,1896974,590,1168035,818,1917887,1471398,1917867,185291,1852362,259063,1852384,1487921,271420,1262777,1262791,1262793,1262834,1262816,177400,1286,1262911,140626,1262966,1262957,1495,1497,46680,83554,83557,1517682,288966,301301,1812858,239974,239984,1444231,186813,39497,166486,1780378,1235793,1002526,2108523,265477,1408306,1297750,1895747,28124,2293209,384636,1121961,44749,438033,1261403,1519494,397287,765952,98360,1261634,180311,49338,626930,276,2015554,418240,393762,1499689,1032851,185007,29360,29364,2183912,29385,377615,2184082,46126,46205,1262771,857252,1856685,1262796,1262843,1282,156974,1262938,169292,1262919,1262920,1262963,386414,38305,681398,1563,1644118,1410633,1410632,1115758,136843,716544,765952,280236,2086575,1734396,349938,747377,1972128,108486,329854,1583341,1792306,1673718,1280676,1981174,404335,39791,89065,105483,380021,48256,1871012,2161820,2292964,1658110,425254};
        //int[] taxids = new int[] {765952,716544 };
        for (int taxid : taxids) {
            TAXONOMIC_RANK rank = IndexAssessmentTask.getClosestRelevantRank(taxonomy.getTaxon(taxid));
            System.out.println(taxid + "\t" + rank.getString());
        }
    }
}
