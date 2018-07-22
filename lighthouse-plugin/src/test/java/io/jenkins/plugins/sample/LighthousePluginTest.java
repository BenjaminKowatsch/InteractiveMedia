package io.jenkins.plugins.sample;

import org.json.JSONObject;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class LighthousePluginTest {

    @Test
    public void Test_PositiveEquals(){

        final String path = "reportCategories/Array/0/Object/score/Double";
        final String action = "eq";
        final String value = "61.94117647058823";
        final String filepath = "work/jobs/test/workspace/report.json";
        final String failStatus = "UNSTABLE";
        final LighthousePlugin plugin = new LighthousePlugin(path,value, action, filepath, failStatus);
        try {
            final JSONObject object = plugin.parseLighthouseReport(filepath);
            System.out.println(object);
            assertEquals(true, plugin.analyseLighthouseReport(object, new PrintStream(System.out)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Test_NegativeEquals(){

        final String path = "reportCategories/Array/0/Object/score/Double";
        final String action = "eq";
        final String value = "655.94117647058823";
        final String filepath = "work/jobs/test/workspace/report.json";
        final String failStatus = "UNSTABLE";
        final LighthousePlugin plugin = new LighthousePlugin(path,value, action, filepath, failStatus);
        try {
            final JSONObject object = plugin.parseLighthouseReport(filepath);
            System.out.println(object);
            assertEquals(false, plugin.analyseLighthouseReport(object, new PrintStream(System.out)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Test_PositiveLowerThan(){

        final String path = "reportCategories/Array/0/Object/score/Double";
        final String action = "lt";
        final String value = "655.94117647058823";
        final String filepath = "work/jobs/test/workspace/report.json";
        final String failStatus = "UNSTABLE";
        final LighthousePlugin plugin = new LighthousePlugin(path,value, action, filepath, failStatus);
        try {
            final JSONObject object = plugin.parseLighthouseReport(filepath);
            System.out.println(object);
            assertEquals(true, plugin.analyseLighthouseReport(object, new PrintStream(System.out)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void Test_PositiveGreaterThan(){

        final String path = "reportCategories/Array/0/Object/score/Double";
        final String action = "gt";
        final String value = "5.94117647058823";
        final String filepath = "work/jobs/test/workspace/report.json";
        final String failStatus = "UNSTABLE";
        final LighthousePlugin plugin = new LighthousePlugin(path,value, action, filepath, failStatus);
        try {
            final JSONObject object = plugin.parseLighthouseReport(filepath);
            System.out.println(object);
            assertEquals(true, plugin.analyseLighthouseReport(object, new PrintStream(System.out)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}