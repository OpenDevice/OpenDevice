/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.util.math;

import br.com.criativasoft.opendevice.core.model.DeviceHistory;

import java.util.LinkedList;
import java.util.List;

/**
 * Naive implementation of down sample with simple array input.
 * Copes gracefully with nulls by preserving them.
 * The algorithm (called Largest-Triangle-Three-Buckets or LTTB) used in this plugin is described in a Master's thesis (see hdl.handle.net/1946/15343)
 *
 * @author Benjamin Green
 */
public class DownsampleLTTB {

    public static List<DeviceHistory> execute(List<DeviceHistory> data, Integer threshold) {
        Number[][] raw = new Number[data.size()][2];
        List<DeviceHistory> sampled = new LinkedList<DeviceHistory>();

        int index = 0;
        for (DeviceHistory history : data) {
            raw[index][0] = history.getTimestamp();
            raw[index][1] = history.getValue();
            index++;
        }

        Number[][] buckets = largestTriangleThreeBuckets(raw, threshold);

        for (int i = 0; i < buckets.length; i++) {
            sampled.add(new DeviceHistory((Long) buckets[i][0], (Double) buckets[i][1]));
        }

        return sampled;
    }

    public static Number[][] largestTriangleThreeBuckets(Number[][] data, Integer threshold) {
        Number[][] sampled = new Number[threshold][];
        if (data == null) {
            throw new NullPointerException("Cannot cope with a null data input array.");
        }
        if (threshold <= 2) {
            throw new RuntimeException("What am I supposed to do with that?");
        }
        if (data.length <= 2 || data.length <= threshold) {
            return data;
        }
        int sampled_index = 0;
        double every = (double) (data.length - 2) / (double) (threshold - 2);
        int a = 0, next_a = 0;
        Number[] max_area_point = null;
        double max_area, area;

        sampled[sampled_index++] = data[a];

        for (int i = 0; i < threshold - 2; i++) {
            double avg_x = 0.0D, avg_y = 0.0D;
            int avg_range_start = (int) Math.floor((i + 1) * every) + 1;
            int avg_range_end = (int) Math.floor((i + 2) * every) + 1;
            avg_range_end = avg_range_end < data.length ? avg_range_end : data.length;
            int avg_range_length = (int) (avg_range_end - avg_range_start);
            while (avg_range_start < avg_range_end) {
                avg_x = avg_x + data[avg_range_start][0].doubleValue();
                avg_y += data[avg_range_start][1].doubleValue();
                avg_range_start++;
            }
            avg_x /= avg_range_length;
            avg_y /= avg_range_length;

            int range_offs = (int) Math.floor((i + 0) * every) + 1;
            int range_to = (int) Math.floor((i + 1) * every) + 1;

            double point_a_x = data[a][0].doubleValue();
            double point_a_y = data[a][1].doubleValue();

            max_area = area = -1;

            while (range_offs < range_to) {
                area = Math.abs(
                        (point_a_x - avg_x) * (data[range_offs][1].doubleValue() - point_a_y) -
                                (point_a_x - data[range_offs][0].doubleValue()) * (avg_y - point_a_y)
                ) * 0.5D;
                if (area > max_area) {
                    max_area = area;
                    max_area_point = data[range_offs];
                    next_a = range_offs;
                }
                range_offs++;
            }
            sampled[sampled_index++] = max_area_point;
            a = next_a;
        }

        sampled[sampled_index++] = data[data.length - 1];
        return sampled;
    }

    public static Number[][] largestTriangleThreeBucketsTime(Number[][] data, Integer threshold) {
        Number[][] sampled = new Number[threshold][];
        if (data == null) {
            throw new NullPointerException("Cannot cope with a null data input array.");
        }
        if (threshold <= 2) {
            throw new RuntimeException("What am I supposed to do with that?");
        }
        if (data.length <= 2 || data.length <= threshold) {
            return data;
        }
        int bucket_interval = (int) ((data[data.length - 1][0].longValue() - data[0][0].longValue()) / threshold);
        int sampled_index = 0;
        double every = (double) (data.length - 2) / (double) (threshold - 2);
        int a = 0, next_a = 0;
        Number[] max_area_point = null;
        double max_area, area;

        sampled[sampled_index++] = data[a];

        for (int i = 0; i < threshold - 2; i++) {
            double avg_x = 0.0D, avg_y = 0.0D;
            int avg_range_start = (int) Math.floor((i + 1) * every) + 1;
            int avg_range_end = (int) Math.floor((i + 2) * every) + 1;
            avg_range_end = avg_range_end < data.length ? avg_range_end : data.length;
            int avg_range_length = (int) (avg_range_end - avg_range_start);
            while (avg_range_start < avg_range_end) {
                avg_x = avg_x + data[avg_range_start][0].doubleValue();
                avg_y += data[avg_range_start][1].doubleValue();
                avg_range_start++;
            }
            avg_x /= avg_range_length;
            avg_y /= avg_range_length;

            int range_offs = (int) Math.floor((i + 0) * every) + 1;
            int range_to = (int) Math.floor((i + 1) * every) + 1;

            double point_a_x = data[a][0].doubleValue();
            double point_a_y = data[a][1].doubleValue();

            max_area = area = -1;

            long ending_time = data[range_offs][0].longValue() + bucket_interval;

            while (range_offs < range_to) { // && data[range_offs][0].longValue() < ending_time) {
                area = Math.abs(
                        (point_a_x - avg_x) * (data[range_offs][1].doubleValue() - point_a_y) -
                                (point_a_x - data[range_offs][0].doubleValue()) * (avg_y - point_a_y)
                ) * 0.5D;
                if (area > max_area) {
                    max_area = area;
                    max_area_point = new Number[]{ending_time, data[range_offs][1]};
                    next_a = range_offs;
                }
                range_offs++;
            }
            sampled[sampled_index++] = max_area_point;
            a = next_a;
        }

        sampled[sampled_index++] = data[data.length - 1];
        return sampled;
    }
}