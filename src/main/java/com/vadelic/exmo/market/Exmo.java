package com.vadelic.exmo.market;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Exmo {
    private static final String ALGORITHM = "HmacSHA512";
    private static long nonce;
    private final Logger LOG = Logger.getLogger(getClass());
    private final ObjectMapper MAPPER = new ObjectMapper();
    private String key;
    private String secret;
    private static LocalDateTime lastRequest = LocalDateTime.now();

    public  Exmo(String key, String secret) {
        nonce = System.nanoTime();
        this.key = key;
        this.secret = secret;
    }

    private synchronized Object request(String command, Map<String, String> param) throws ExmoRestApiException {
        if (lastRequest.isAfter(LocalDateTime.now().minusSeconds(1))) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        lastRequest = LocalDateTime.now();

        if (param == null) {
            param = new HashMap<>();
        }
        param.put("nonce", "" + ++nonce);  // Add the dummy nonce.

        StringBuilder arguments = new StringBuilder();
        for (Map.Entry<String, String> argument : param.entrySet()) {
            if (arguments.length() != 0)
                arguments.append("&");

            arguments.append(argument.getKey())
                    .append("=")
                    .append(argument.getValue());

        }

        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes("UTF-8"), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKeySpec);
            String sign = Hex.encodeHexString(mac.doFinal(arguments.toString().getBytes("UTF-8")));

            MediaType contentType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

            RequestBody body = RequestBody.create(contentType, arguments.toString());
            Request request = new Request.Builder()
                    .url("https://api.exmo.me/v1/" + command)
                    .addHeader("Key", this.key)
                    .addHeader("Sign", sign)
                    .post(body)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(request);
            Response response = call.execute();

            String string = "{}";
            final int code = response.code();
            try (final ResponseBody responseBody = response.body()) {
                if (code == 200) {
                    string = responseBody.string();
                }
            }

            Object responseObject = MAPPER.readValue(string, Object.class);

            checkError(responseObject);
            return responseObject;


        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {

            throw new ExmoRestApiException(e.getMessage());
        }
    }

    private void checkError(Object responseObject) throws ExmoRestApiException {
        if (responseObject instanceof Map) {
            if (((Map) responseObject).get("result") != null) {
                if (Objects.equals(((Map) responseObject).get("result"), false)) {
                    throw new ExmoRestApiException((String) ((Map) responseObject).get("error"));
                }
            }

        }
    }

    private String getCurrencyPairParam(String pair, String[] pairs) {

        Stream<String> stream = Arrays.stream(pairs);
        if (pair != null) {
            stream = Stream.concat(stream, Stream.of(pair));

        }
        return stream.map(Object::toString).collect(Collectors.joining(","));
    }

    /**
     * Getting information about user's account
     *
     * @throws ExmoRestApiException
     */
    public Map<String, Object> userInfo() throws ExmoRestApiException {
        return MAPPER.convertValue(request("user_info", null), new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Getting the list of user’s cancelled orders
     *
     * @return 100 orders without offset
     * @throws ExmoRestApiException
     */
    public ArrayList userCancelledOrders() throws ExmoRestApiException {
        return userCancelledOrders(null, null);
    }

    /**
     * Getting the list of user’s cancelled orders
     *
     * @param limit  the number of returned deals (default: 100, мmaximum: 10 000)
     * @param offset last deal offset (default: 0)
     * @throws ExmoRestApiException
     */
    public ArrayList userCancelledOrders(@Nullable Integer limit, @Nullable Integer offset) throws
            ExmoRestApiException {
        HashMap<String, String> param = new HashMap<>();
        if (limit != null) param.put("limit", String.valueOf(limit));
        if (offset != null) param.put("offset", String.valueOf(offset));
        return (ArrayList) request("user_cancelled_orders", param);

    }

    /**
     * List of the deals on currency pairs
     *
     * @param pair  currency pair
     * @param pairs more than one currency pairs
     * @throws ExmoRestApiException
     */
    public Map stockTradeDeals(String pair, String... pairs) throws ExmoRestApiException {
        HashMap<String, String> param = new HashMap<>();
        param.put("pair", getCurrencyPairParam(pair, pairs));
        return (Map) request("trades", param);

    }

    /**
     * Getting the list of user’s active orders
     *
     * @throws ExmoRestApiException
     */
    public Map<String, List<Map<String, Object>>> openOrders() throws ExmoRestApiException {
        return MAPPER.convertValue(request("user_open_orders", null), new TypeReference<Map<String, List<Map<String, Object>>>>() {
        });
    }

    /**
     * Getting the list of user’s deals
     *
     * @param pair  currency pair
     * @param pairs more than one currency pairs
     * @return 100 deals without offset
     * @throws ExmoRestApiException
     */
    public Map completeTrades(String pair, String... pairs) throws ExmoRestApiException {
        return completeTrades(null, null, pair, pairs);
    }

    /**
     * Getting the list of user’s deals
     *
     * @param limit  the number of returned deals (default: 100, мmaximum: 10 000)
     * @param offset last deal offset (default: 0)
     * @param pair   currency pair
     * @param pairs  more than one currency pairs
     * @throws ExmoRestApiException
     */
    public Map completeTrades(@Nullable Integer limit, @Nullable Integer offset, String pair, String... pairs) throws
            ExmoRestApiException {

        HashMap<String, String> param = new HashMap<>();
        if (limit != null) param.put("limit", String.valueOf(limit));
        if (offset != null) param.put("offset", String.valueOf(offset));
        param.put("pair", getCurrencyPairParam(pair, pairs));
        return (Map) request("user_trades", param);
    }

    /**
     * Getting the history of deals with the order
     *
     * @param orderId
     * @return Load trades to order and return one
     * @throws ExmoRestApiException
     */
    public Map<String, Object> orderTrades(Integer orderId) throws ExmoRestApiException {
        HashMap<String, String> param = new HashMap<>();
        param.put("order_id", String.valueOf(orderId));
        return MAPPER.convertValue(request("order_trades", param), new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Getting the list of addresses for cryptocurrency deposit
     *
     * @throws ExmoRestApiException
     */
    public Map userDepositAddress() throws ExmoRestApiException {
        return (Map) request("deposit_address", null);
    }

    /**
     * Calculating the sum of buying a certain amount of currency for the particular currency pair
     *
     * @param pair     currency pair
     * @param quantity quantity to buy
     * @return
     * @throws ExmoRestApiException
     */
    public Map calcAmount(String pair, Double quantity) throws ExmoRestApiException {
        HashMap<String, String> param = new HashMap<>();
        param.put("pair", pair);
        param.put("quantity", String.valueOf(quantity));
        return (Map) request("required_amount", param);
    }

    /**
     * Statistics on prices and volume of trades by currency pairs
     *
     * @throws ExmoRestApiException
     */
    public Map<String, Map<String, Double>> tickerStat() throws ExmoRestApiException {
        Object response = request("ticker", null);
        return MAPPER.convertValue(response, new TypeReference<Map<String, Map<String, Double>>>() {
        });
    }

    /**
     * Order cancellation
     *
     * @param orderId
     * @throws ExmoRestApiException
     */
    public boolean cancelOrder(int orderId) throws ExmoRestApiException {
        HashMap<String, String> param = new HashMap<>();
        param.put("order_id", String.valueOf(orderId));
        request("order_cancel", param);
        return true;
    }

    /**
     * Order creation
     *
     * @param pair - currency pair
     *             quantity - quantity for the order
     *             price - price for the order
     *             type - type of order, can have the following values:
     * @return true and set orderId to order
     * @throws ExmoRestApiException
     */
    public int createOrder(String pair, double quantity, double price, String type) throws ExmoRestApiException {
        HashMap<String, String> param = new HashMap<>();
        param.put("pair", pair);
        param.put("quantity", String.valueOf(quantity));
        param.put("price", String.valueOf(price));
        param.put("type", type);

        Map response = (Map) request("order_create", param);
        return (int) response.get("order_id");
    }


    /**
     * Currency pairs settings
     */
    public Map<String, Map<String, Double>> pairSettings() throws ExmoRestApiException {
        Object response = request("pair_settings", null);
        return MAPPER.convertValue(response, new TypeReference<Map<String, Map<String, Double>>>() {
        });
    }


    public Map<String, Map> stockOrderBook(Integer limit, String pair, String... pairs) throws ExmoRestApiException {
        HashMap<String, String> param = new HashMap<>();
        if (limit != null)
            param.put("limit", String.valueOf(limit));
        param.put("pair", getCurrencyPairParam(pair, pairs));
        Object response = request("order_book", param);
        return MAPPER.convertValue(response, new TypeReference<Map<String, Map>>() {
        });

    }

    public Map stockCurrency() throws ExmoRestApiException {
        return (Map) request("currency", null);

    }


}