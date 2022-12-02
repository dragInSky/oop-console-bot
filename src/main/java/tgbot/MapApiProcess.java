package tgbot;

import tgbot.Exceptions.HttpException;
import tgbot.Exceptions.MapApiException;
import tgbot.Exceptions.ParseException;

import java.text.MessageFormat;
import java.util.Objects;

public class MapApiProcess {
    private static final int RADIUS_OF_SEARCH = 500;
    private static String firstAddr = "", secondAddr = "", middlePointPlaceAddress;
    private static Coordinates firstCoordinates = null, secondCoordinates = null, middlePoint;
    private static boolean repeatCommand = false, middlePointOnMap = false;
    //private static int duration;
    //private static boolean button = false;
    private final HttpRequest httpRequest = new HttpRequest();
    private final Parser parser = new Parser();

    public boolean getRepeatCommand(){
        return repeatCommand;
    }
    public boolean getMiddlePointOnMap(){
        return middlePointOnMap;
    }
    //public static boolean getButton() { return button; }
    //public int getDuration() { return duration; }

    public void resetValues() {
        repeatCommand = false;
        firstAddr = "";
        secondAddr = "";
    }

    private Coordinates addressToCoordinates(String addr) throws HttpException, MapApiException, ParseException {
        String url = MessageFormat.format(
                "https://catalog.api.2gis.com/3.0/items/geocode?q={0}&fields=items.point&key={1}",
                addr, get2GisGetKey());
        String response = httpRequest.sendGet(url);
        String code = parser.findCode(response);
        if (!Objects.equals(code, "200")) {
            throw new MapApiException("������ ������������ �����: " + addr);
        }
        return parser.findCoordinates(response);
    }

//    private String coordinatesToAddress(Coordinates point) throws HttpException, MapApiException, ParseException {
//        String url = MessageFormat.format(
//                "https://catalog.api.2gis.com/3.0/items/geocode?lat={0}&lon={1}&fields=items.point&key={2}",
//                point.getLat() + "", point.getLon() + "", get2GisGetKey());
//        String response = httpRequest.sendGet(url);
//        String code = parser.findCode(response);
//        if (!Objects.equals(code, "200")) {
//            throw new MapApiException(code);
//        }
//        return parser.findAddress(response);
//    }

    public String createRouteWithAddress(String addr, SearchCategories search)
            throws HttpException, MapApiException, ParseException {
        String url = MessageFormat.format(
                "https://routing.api.2gis.com/carrouting/6.0.1/global?key={0}",
                get2GisPostKey());

        if (Objects.equals(addr, "")) {
            repeatCommand = true;
            return "������� ������ �����";
        }
        else if (Objects.equals(firstAddr, "")) {
            firstAddr = addr;
            firstCoordinates = addressToCoordinates(firstAddr);
            return "������� ������ �����";
        }
        else if (Objects.equals(secondAddr, "")) {
            secondAddr = addr;
            secondCoordinates = addressToCoordinates(secondAddr);
        }

        if (Objects.equals(firstAddr, secondAddr)) {
            throw new MapApiException("������� ������ ������!");
        }
        resetValues();

        String response = httpRequest.sendPost(url, firstCoordinates, secondCoordinates);
        if (Objects.equals(response, "")) { //�� ������ �������, ����� ��� ������� �����������
            throw new MapApiException("������� �� ����� ���� ��������!");
        }
        String status = parser.findStatus(response);
        if (!status.equals("OK")) {
            throw new MapApiException("������: " + status);
        }

        //����� ��� ������ ������� �����
        //duration = parser.findDuration(response);
        middlePoint = new CoordinatesProcessor(response, firstCoordinates, secondCoordinates).
                coordinatesProcessEconom();
        middlePointOnMap = true;
        String middlePointPlace = radiusSearch(search);

        return parser.findRouteInformation(response) + "\n" + middlePointPlace; // ����� ������� �����
    }

    public String radiusSearch(SearchCategories search) throws HttpException, ParseException {
        String url = MessageFormat.format(
                "https://catalog.api.2gis.com/3.0/items?q={0}&type=branch&point={1}%2C{2}&radius={3}&sort=rating&key={4}",
                search.getSearch(), middlePoint.getLon() + "", middlePoint.getLat() + "", RADIUS_OF_SEARCH, get2GisGetKey());
        String response = httpRequest.sendGet(url);
        middlePointPlaceAddress = parser.findPlaceAddress(response);
        return "����� �������: " + middlePointPlaceAddress +
                " � " + parser.findPlaceInfo(response);
    }

//    public String createRouteWithCoordinates(Coordinates coordinates) throws HttpException { //����� ��� ������ ������� �����
//        String url = MessageFormat.format(
//                "https://routing.api.2gis.com/carrouting/6.0.1/global?key={0}",
//                get2GisPostKey());
//        String response = httpRequest.sendPost(url, firstCoordinates, coordinates);
//        if (response == null) {
//            return "����������� ������";
//        }
//        return response;
//    }

    public String mapDisplay(String token, String id, String addr) throws HttpException, MapApiException, ParseException {
        if (Objects.equals(addr, "")) {
            repeatCommand = true;
            return "������� �����";
        }
        else {
            repeatCommand = false;
        }

        Coordinates coordinates = addressToCoordinates(addr);
        String url = MessageFormat.format(
                "https://api.telegram.org/bot{0}/sendlocation?chat_id={1}&latitude={2}&longitude={3}",
                token, id, coordinates.getLat() + "", coordinates.getLon() + "");
        httpRequest.sendGet(url);

        return null;
    }

    public void coordinatesMapDisplay(String token, String id) throws HttpException, MapApiException, ParseException {
        middlePointOnMap = false;
        Coordinates coordinates = addressToCoordinates(middlePointPlaceAddress);
        String url = MessageFormat.format(
                "https://api.telegram.org/bot{0}/sendlocation?chat_id={1}&latitude={2}&longitude={3}",
                token, id, coordinates.getLat() + "", coordinates.getLon() + "");
        httpRequest.sendGet(url);
    }

    public String addrInfo(String addr) throws HttpException, MapApiException, ParseException {
        if (Objects.equals(addr, "")) {
            repeatCommand = true;
            return "������� �����";
        }
        else {
            repeatCommand = false;
        }

        String url = MessageFormat.format(
                "https://catalog.api.2gis.com/3.0/items?building_id={0}&key={1}",
                buildingId(addr), get2GisGetKey());
        String response = httpRequest.sendGet(url);
        String code = parser.findCode(response);
        if (!Objects.equals(code, "200")) {
            throw new MapApiException(code);
        }

        return "�������� �����: " +
                "\n � " + detailAddrInfo(addr) +
                "\n������ �����������: " +
                "\n" + parser.findCompanies(response);
    }

    private String detailAddrInfo(String addr) throws HttpException, MapApiException, ParseException {
        String url = MessageFormat.format(
            "https://catalog.api.2gis.com/3.0/items?q={0}&fields=items.address," +
                    "items.adm_div,items.floors,items.point,items.links,items.structure_info.apartments_count," +
                    "items.structure_info.material,items.structure_info.porch_count&key={1}",
                addr, get2GisGetKey());
        String response = httpRequest.sendGet(url);
        String code = parser.findCode(response);
        if (!Objects.equals(code, "200")) {
            throw new MapApiException(code);
        }

        return parser.findBuildingName(response);
    }

    private String buildingId(String addr) throws HttpException, MapApiException, ParseException {
        String url = MessageFormat.format(
                "https://catalog.api.2gis.com/3.0/items?q={0}&type=building&key={1}",
                addr, get2GisGetKey());
        String response = httpRequest.sendGet(url);
        String code = parser.findCode(response);
        if (!Objects.equals(code, "200")) {
            throw new MapApiException("������ ������������ �����: " + addr);
        }

        return parser.findBuildingId(response);
    }

    public String get2GisPostKey() {
        return System.getenv("2GIS_POST_KEY");
    }

    public String get2GisGetKey() {
        return System.getenv("2GIS_GET_KEY");
    }
}
