import java.util.*;
import java.math.BigInteger;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public class HashiraPlacement {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java HashiraPlacement <json-file-path>");
            return;
        }
        
        String jsonFilePath = args[0];
        try {
            String result = solve(jsonFilePath);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static String solve(String jsonFilePath) {
        try {
            String jsonContent = readFile(jsonFilePath);
            Map<String, String> data = parseJson(jsonContent);
            
            int n = Integer.parseInt(data.get("n"));
            int k = Integer.parseInt(data.get("k"));
            
            List<Point> points = new ArrayList<>();
            
            // Collect all available points
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
            
            // Sort points by x value and take first k points
            points.sort((a, b) -> a.x.compareTo(b.x));
            List<Point> selectedPoints = points.subList(0, Math.min(k, points.size()));
            
            // Use Lagrange interpolation to find the constant term (secret)
            BigInteger secret = lagrangeInterpolation(selectedPoints, BigInteger.ZERO);
            
            return secret.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }
    
    public static String readFile(String filePath) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    public static Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();
        
        try {
            // Parse n and k
            int nStart = json.indexOf("\"n\":");
            if (nStart != -1) {
                int nValueStart = json.indexOf(":", nStart) + 1;
                int nValueEnd = json.indexOf(",", nValueStart);
                if (nValueEnd == -1) nValueEnd = json.indexOf("}", nValueStart);
                String nValue = json.substring(nValueStart, nValueEnd).trim().replace("\"", "");
                result.put("n", nValue);
            }
            
            int kStart = json.indexOf("\"k\":");
            if (kStart != -1) {
                int kValueStart = json.indexOf(":", kStart) + 1;
                int kValueEnd = json.indexOf("}", kValueStart);
                String kValue = json.substring(kValueStart, kValueEnd).trim().replace("\"", "");
                result.put("k", kValue);
            }
            
            // Parse numbered entries
            for (int num = 1; num <= 20; num++) {
                String numKey = "\"" + num + "\":";
                int numStart = json.indexOf(numKey);
                if (numStart != -1) {
                    int objStart = json.indexOf("{", numStart);
                    int objEnd = findMatchingBrace(json, objStart);
                    
                    if (objStart != -1 && objEnd != -1) {
                        String objectContent = json.substring(objStart + 1, objEnd);
                        
                        // Parse base
                        int baseStart = objectContent.indexOf("\"base\":");
                        if (baseStart != -1) {
                            int baseValueStart = objectContent.indexOf(":", baseStart) + 1;
                            int baseValueEnd = objectContent.indexOf(",", baseValueStart);
                            if (baseValueEnd == -1) baseValueEnd = objectContent.indexOf("}", baseValueStart);
                            String baseValue = objectContent.substring(baseValueStart, baseValueEnd)
                                    .trim().replace("\"", "");
                            result.put(num + ".base", baseValue);
                        }
                        
                        // Parse value
                        int valueStart = objectContent.indexOf("\"value\":");
                        if (valueStart != -1) {
                            int valueValueStart = objectContent.indexOf(":", valueStart) + 1;
                            int valueValueEnd = objectContent.indexOf("}", valueValueStart);
                            String valueValue = objectContent.substring(valueValueStart, valueValueEnd)
                                    .trim().replace("\"", "");
                            result.put(num + ".value", valueValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JSON parsing error: " + e.getMessage());
        }
        
        return result;
    }
    
    private static int findMatchingBrace(String str, int start) {
        int count = 1;
        for (int i = start + 1; i < str.length(); i++) {
            if (str.charAt(i) == '{') {
                count++;
            } else if (str.charAt(i) == '}') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
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
    
    static class Point {
        BigInteger x, y;
        
        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "Point(" + x + ", " + y + ")";
        }
    }
}