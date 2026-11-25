package config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight, zero-dependency JSON parser for engine configuration files.
 * Supports standard JSON objects, arrays, strings, numbers, booleans, and null.
 */
public final class JsonParser {
    private final String src;
    private int pos;

    private JsonParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    public static Object parse(Path file) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);
        return parse(content);
    }

    public static Object parse(String json) {
        JsonParser p = new JsonParser(json);
        p.skipWhitespace();
        Object val = p.readValue();
        p.skipWhitespace();
        return val;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object o) {
        if (o instanceof List) return (List<Object>) o;
        return new ArrayList<>();
    }

    public static String str(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return (v != null) ? v.toString() : def;
    }

    public static int intVal(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    public static double dblVal(Map<String, Object> map, String key, double def) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
            try { return Double.parseDouble((String) v); } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    public static boolean boolVal(Map<String, Object> map, String key, boolean def) {
        Object v = map.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) return Boolean.parseBoolean((String) v);
        return def;
    }

    public static int colorHex(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            String s = ((String) v).trim();
            if (s.startsWith("#")) s = s.substring(1);
            else if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
            try {
                long parsed = Long.parseLong(s, 16);
                return (int) parsed;
            } catch (NumberFormatException ignored) {}
        }
        return def;
    }

    private Object readValue() {
        skipWhitespace();
        if (pos >= src.length()) throw new IllegalArgumentException("Unexpected end of JSON");
        char c = src.charAt(pos);
        if (c == '{') return readObject();
        if (c == '[') return readArray();
        if (c == '"') return readString();
        if (c == 't' || c == 'f') return readBoolean();
        if (c == 'n') return readNull();
        if (c == '-' || (c >= '0' && c <= '9')) return readNumber();
        throw new IllegalArgumentException("Unexpected char '" + c + "' at pos " + pos);
    }

    private Map<String, Object> readObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // '{'
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (pos < src.length()) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object val = readValue();
            map.put(key, val);
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                pos++;
            } else if (next == '}') {
                pos++;
                return map;
            } else {
                throw new IllegalArgumentException("Expected ',' or '}' at pos " + pos);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON object");
    }

    private List<Object> readArray() {
        List<Object> list = new ArrayList<>();
        pos++; // '['
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (pos < src.length()) {
            skipWhitespace();
            list.add(readValue());
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                pos++;
            } else if (next == ']') {
                pos++;
                return list;
            } else {
                throw new IllegalArgumentException("Expected ',' or ']' at pos " + pos);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON array");
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\' && pos < src.length()) {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 <= src.length()) {
                            String hex = src.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        break;
                    default: sb.append(esc); break;
                }
            } else {
                sb.append(c);
            }
        }
        throw new IllegalArgumentException("Unterminated JSON string");
    }

    private Number readNumber() {
        int start = pos;
        boolean isDouble = false;
        if (peek() == '-') pos++;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c >= '0' && c <= '9') pos++;
            else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                if (c == '.' || c == 'e' || c == 'E') isDouble = true;
                pos++;
            } else break;
        }
        String raw = src.substring(start, pos);
        if (isDouble) return Double.parseDouble(raw);
        long l = Long.parseLong(raw);
        if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
        return l;
    }

    private Boolean readBoolean() {
        if (src.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        }
        if (src.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Invalid boolean at pos " + pos);
    }

    private Object readNull() {
        if (src.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new IllegalArgumentException("Invalid null at pos " + pos);
    }

    private void skipWhitespace() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
            else break;
        }
    }

    private char peek() {
        return (pos < src.length()) ? src.charAt(pos) : '\0';
    }

    private void expect(char expected) {
        if (pos >= src.length() || src.charAt(pos) != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at pos " + pos);
        }
        pos++;
    }
}
