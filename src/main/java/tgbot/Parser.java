package tgbot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Parser {
    public String findCode(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject meta = json.getJSONObject("meta");
            if (meta.getInt("code") == 200) {
                return "200";
            }
            JSONObject error = meta.getJSONObject("error");
            return error.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public int findDuration(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONArray("result").getJSONObject(0);
            return result.getInt("total_duration");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public String findStatus(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            return json.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public String findBuildingId(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONObject("result");
            JSONObject items = result.getJSONArray("items").getJSONObject(0);
            if (items.has("id")) {
                return items.getString("id");
            }
            throw new BotException("�� ����� ������ ��� ������ �����������");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public String findCompanies(String response) throws BotException {
        try {
            StringBuilder companies = new StringBuilder();
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONObject("result");
            JSONArray items = result.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject company = items.getJSONObject(i);
                if (company.has("name")) {
                    companies.append(" - ").append(company.getString("name")).append("\n");
                }
            }

            if (!companies.isEmpty()) {
                return companies.toString();
            }
            throw new BotException("�� ����� ������ ��� ������ �����������");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public String findRouteInformation(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONArray("result").getJSONObject(0);
            JSONObject distance = result.getJSONObject("ui_total_distance");
            return "���������� ��������: " + distance.getString("value") + " " + distance.getString("unit") +
                    "\n������������ ��������: " + result.getString("ui_total_duration");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public Coordinates findCoordinates(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONObject("result");
            JSONObject items = result.getJSONArray("items").getJSONObject(0);
            JSONObject point = items.getJSONObject("point");
            return new Coordinates(point.getDouble("lat"), point.getDouble("lon"));
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public String findBuildingName(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONObject("result");
            JSONObject items = result.getJSONArray("items").getJSONObject(0);
            if (items.has("building_name")) {
                return items.getString("building_name");
            }
            throw new BotException("�� ����� ������ ��� ������ �����������");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }

    public String findAddress(String response) throws BotException {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONObject("result");
            JSONObject items = result.getJSONArray("items").getJSONObject(0);
            return items.getString("full_name");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new BotException("������ �� ������� ������������!");
        }
    }
}
