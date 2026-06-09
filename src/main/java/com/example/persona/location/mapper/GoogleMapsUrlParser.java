package com.example.persona.location.mapper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts latitude/longitude from Google Maps URLs so users can paste a link
 * instead of manually looking up coordinates.
 *
 * Supported formats:
 *   https://www.google.com/maps/place/Name/@11.5564,104.9282,17z
 *   https://www.google.com/maps/@11.5564,104.9282,15z
 *   https://www.google.com/maps?q=11.5564,104.9282
 *   https://maps.google.com/?q=11.5564,104.9282
 *   https://www.google.com/maps/place/.../@11.5564,104.9282,...
 */
public final class GoogleMapsUrlParser {

    // @lat,lng,zoom  — the most common form copied from the browser address bar
    private static final Pattern AT_PATTERN = Pattern.compile("@(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)");

    // ?q=lat,lng  or  &q=lat,lng
    private static final Pattern Q_PARAM_PATTERN = Pattern.compile("[?&]q=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)");

    // !3d<lat>!4d<lng>  — embedded in /data= paths
    private static final Pattern DATA_PATTERN = Pattern.compile("!3d(-?\\d+\\.\\d+)!4d(-?\\d+\\.\\d+)");

    private GoogleMapsUrlParser() {}

    public record LatLng(double latitude, double longitude) {}

    /**
     * Returns the first lat/lng pair found in the URL, or empty if none can be extracted.
     */
    public static Optional<LatLng> parse(String url) {
        if (url == null || url.isBlank()) return Optional.empty();

        String decoded;
        try {
            decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
        } catch (Exception e) {
            decoded = url;
        }

        // Try @lat,lng first (most precise — it's what the map is centred on)
        Optional<LatLng> result = tryMatch(AT_PATTERN, decoded);
        if (result.isPresent()) return result;

        // Try ?q=lat,lng
        result = tryMatch(Q_PARAM_PATTERN, decoded);
        if (result.isPresent()) return result;

        // Try !3d<lat>!4d<lng> data encoding
        return tryMatch(DATA_PATTERN, decoded);
    }

    private static Optional<LatLng> tryMatch(Pattern pattern, String url) {
        Matcher m = pattern.matcher(url);
        if (!m.find()) return Optional.empty();
        try {
            double lat = Double.parseDouble(m.group(1));
            double lng = Double.parseDouble(m.group(2));
            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) return Optional.empty();
            return Optional.of(new LatLng(lat, lng));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
