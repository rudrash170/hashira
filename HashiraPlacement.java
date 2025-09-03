import java.util.*;
import java.math.BigInteger;

public class HashiraPlacement {
    
    public static void main(String[] args) {
        // Test case 1
        String testCase1 = """
        {
            "keys": {
                "n": 4,
                "k": 3
            },
            "1": {
                "base": "10",
                "value": "4"
            },
            "2": {
                "base": "2",
                "value": "111"
            },
            "3": {
                "base": "10",
                "value": "12"
            },
            "6": {
                "base": "4",
                "value": "213"
            }
        }
        """;
        
        // Test case 2
        String testCase2 = """
        {
            "keys": {
                "n": 10,
                "k": 7
            },
            "1": {
                "base": "6",
                "value": "13444211440455345511"
            },
            "2": {
                "base": "15",
                "value": "aed7015a346d635"
            },
            "3": {
                "base": "15",
                "value": "6aeeb69631c227c"
            },
            "4": {
                "base": "16",
                "value": "e1b5e05623d881f"
            },
            "5": {
                "base": "8",
                "value": "316034514573652620673"
            },
            "6": {
                "base": "3",
                "value": "2122212201122002221120200210011020220200"
            },
            "7": {
                "base": "3",
                "value": "20120221122211000100210021102001201112121"
            },
            "8": {
                "base": "6",
                "value": "20220554335330240002224253"
            },
            "9": {
                "base": "12",
                "value": "45153788322a1255483"
            },
            "10": {
                "base": "7",
                "value": "1101613130313526312514143"
            }
        }
        """;
        
        System.out.println(solve(testCase1));
        System.out.println(solve(testCase2));
    }
    
    public static BigInteger solve(String jsonInput) {
        try {
            Map<String, String> data = parseJson(jsonInput);
            
            int n = Integer.parseInt(data.get("keys.n"));
            int k = Integer.parseInt(data.get("keys.k"));
            
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
        
        // Remove all whitespace and newlines for easier parsing
        json = json.replaceAll("\\s+", "");
        
        // Parse n and k values
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
        
        // Parse numbered entries using regex-like approach
        for (int i = 1; i <= 20; i++) { // Check up to 20 possible keys
            String keyPattern = "\"" + i + "\":{";
            int keyIndex = json.indexOf(keyPattern);
            if (keyIndex != -1) {
                int endIndex = json.indexOf("}", keyIndex);
                if (endIndex != -1) {
                    String section = json.substring(keyIndex, endIndex + 1);
                    
                    // Extract base
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
                    
                    // Extract value
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