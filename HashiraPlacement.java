import java.util.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HashiraPlacement {

    public static void main(String[] args) throws Exception {
        String testCase1 = Files.readString(Paths.get("testcase1.json"));
        String testCase2 = Files.readString(Paths.get("testcase2.json"));

        System.out.println(solve(testCase1));
        System.out.println(solve(testCase2));
    }

    public static BigInteger solve(String jsonInput) {
        try {
            Map<String, String> data = parseJson(jsonInput);

            int n = Integer.parseInt(data.get("keys.n"));
            int k = Integer.parseInt(data.get("keys.k"));

            List<Point> points = new ArrayList<>();

            for (int i = 1; i <= n; i++) {
                String baseKey = i + ".base";
                String valueKey = i + ".value";

                if (data.containsKey(baseKey) && data.containsKey(valueKey)) {
                    int base = Integer.parseInt(data.get(baseKey));
                    String value = data.get(valueKey);

                    BigInteger y = convertToDecimal(value, base);
                    points.add(new Point(BigInteger.valueOf(i), y));
                }
            }

            points.sort((a, b) -> a.x.compareTo(b.x));
            List<Point> selectedPoints = points.subList(0, Math.min(k, points.size()));

            return lagrangeInterpolation(selectedPoints, BigInteger.ZERO);

        } catch (Exception e) {
            return BigInteger.ZERO;
        }
    }

    public static BigInteger convertToDecimal(String value, int base) {
        return new BigInteger(value, base);
    }

    public static BigInteger lagrangeInterpolation(List<Point> points, BigInteger x) {
        BigInteger result = BigInteger.ZERO;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    numerator = numerator.multiply(x.subtract(points.get(j).x));
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }

            BigInteger term = points.get(i).y.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    public static Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();
        json = json.replaceAll("\\s+", "");

        String[] nMatch = json.split("\"n\":");
        if (nMatch.length > 1) {
            String nValue = nMatch[1].split(",")[0];
            result.put("keys.n", nValue);
        }

        String[] kMatch = json.split("\"k\":");
        if (kMatch.length > 1) {
            String kValue = kMatch[1].split("}")[0];
            result.put("keys.k", kValue);
        }

        for (int i = 1; i <= 20; i++) {
            String keyPattern = "\"" + i + "\":{";
            int keyIndex = json.indexOf(keyPattern);
            if (keyIndex != -1) {
                int endIndex = json.indexOf("}", keyIndex);
                if (endIndex != -1) {
                    String section = json.substring(keyIndex, endIndex + 1);

                    String basePattern = "\"base\":\"";
                    int baseIndex = section.indexOf(basePattern);
                    if (baseIndex != -1) {
                        int baseStart = baseIndex + basePattern.length();
                        int baseEnd = section.indexOf("\"", baseStart);
                        if (baseEnd != -1) {
                            String base = section.substring(baseStart, baseEnd);
                            result.put(i + ".base", base);
                        }
                    }

                    String valuePattern = "\"value\":\"";
                    int valueIndex = section.indexOf(valuePattern);
                    if (valueIndex != -1) {
                        int valueStart = valueIndex + valuePattern.length();
                        int valueEnd = section.indexOf("\"", valueStart);
                        if (valueEnd != -1) {
                            String value = section.substring(valueStart, valueEnd);
                            result.put(i + ".value", value);
                        }
                    }
                }
            }
        }

        return result;
    }

    static class Point {
        BigInteger x, y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
}