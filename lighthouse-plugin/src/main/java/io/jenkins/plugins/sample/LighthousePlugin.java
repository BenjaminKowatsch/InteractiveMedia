package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.*;
import java.util.Arrays;

public class LighthousePlugin extends Builder implements SimpleBuildStep {

    private String filepath;
    private String value;
    private String action;
    private Action actionEnum;
    private String path;
    private String failStatus = "";

    public enum Action{
        EQUAL,
        LOWER_THAN,
        GREATER_THAN,
    }

    @DataBoundConstructor
    public LighthousePlugin(String path, String value, String action, String filepath, String failStatus) {
        this.path = path;
        this.value = value;
        this.filepath = filepath;
        this.action = action;
        this.failStatus = failStatus == null ? "": failStatus;
        if(action.toLowerCase().equals("eq")){
            this.actionEnum = Action.EQUAL;
        } else if(action.toLowerCase().equals("gt")){
            this.actionEnum = Action.GREATER_THAN;
        } else { // <
            this.actionEnum = Action.LOWER_THAN;
        }
    }

    public String getPath() {
        return path;
    }
    public String getValue() {
        return value;
    }
    public String getAction() {
        return action;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getFailStatus() {
        return failStatus;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        final String jsonFilepath = workspace.absolutize()+"/" + filepath;
        listener.getLogger().println("Loading JSON file from: " + jsonFilepath);
        final JSONObject object = parseLighthouseReport(jsonFilepath);

        if(analyseLighthouseReport(object, listener.getLogger())) {
            run.setResult(Result.SUCCESS);
        } else{
            listener.getLogger().print("Constraint not valid, setting the status ");
            switch (failStatus.toLowerCase()){
                case "aborted":
                    listener.getLogger().print("ABORTED.\n");
                    run.setResult(Result.ABORTED);
                    break;
                case "failure":
                    listener.getLogger().print("FAILURE.\n");
                    run.setResult(Result.FAILURE);
                    break;
                default:
                case "unstable":
                    listener.getLogger().print("UNSTABLE.\n");
                    run.setResult(Result.UNSTABLE);
                    break;
            }
        }
    }

    public JSONObject parseLighthouseReport(String filepath) throws FileNotFoundException {
        final InputStream inputStream = new FileInputStream(new File(filepath));
        final JSONTokener tokener = new JSONTokener(inputStream);
        final JSONObject root = new JSONObject(tokener);
        return root;
    }

    public boolean analyseLighthouseReport(JSONObject root, PrintStream logger){
        final String[] elements = path.split("/");
        final String finalType = elements[elements.length-1];
        final Object object = find(elements, root);
        final boolean result = compare(object, actionEnum, value, finalType);
        logger.println("The variable of type "+ finalType+ " with the value " + object+ (result ? " is ":" is not ") + actionEnum.toString() + " "+ value);
        return result;
    }

    private boolean compare(Object object, Action action, String value, String finalType){
        switch (finalType.toLowerCase()){
            case "double":
            case "integer":
                double v;
                try{
                    v = ((Double)object).doubleValue();
                } catch(Exception e){
                    v = ((Integer)object).doubleValue();
                }
                switch (action){
                    case EQUAL:
                        return v == Double.parseDouble(value);
                    case LOWER_THAN:
                        return v < Double.parseDouble(value);
                    case GREATER_THAN:
                        return v > Double.parseDouble(value);
                }
            case "boolean":
                switch (action){
                    case EQUAL:
                        return (Boolean) object == Boolean.parseBoolean(value);
                 }
            case "string":
                int compare = ((String)object).compareTo(value);
                switch (action){
                    case EQUAL:
                        return compare == 0;
                    case LOWER_THAN:
                        return compare > 0;
                    case GREATER_THAN:
                        return compare < 0;
                }
            default:
                return false;
        }
    }

    private Object find(String[] elements, JSONObject object){
        if(elements.length >=2){
            String key = elements[0];
            String type = elements[1];
            final String[] remaining = Arrays.copyOfRange(elements, 2, elements.length);
            if(remaining.length > 0){
                if(type.toLowerCase().equals("array")) {
                    final JSONArray next = object.getJSONArray(key);
                    return find(remaining, next);
                } else {
                    final JSONObject next = object.getJSONObject(key);
                    return find(remaining, next);
                }
            } else {
                System.out.println(object.get(key));
                return object.get(key);
            }
        }
        return null;
    }

    private Object find(String[] elements, JSONArray array){
        if(elements.length >=2){
            int index = Integer.parseInt(elements[0]);
            String type = elements[1];
            final String[] remaining = Arrays.copyOfRange(elements, 2, elements.length);
            if(remaining.length > 0){
                if(type.toLowerCase().equals("array")) {
                    final JSONArray next = array.getJSONArray(index);
                    return find(remaining, next);
                } else {
                    final JSONObject next = array.getJSONObject(index);
                    return find(remaining, next);
                }
            } else {
                return array.get(index);
            }
        }
        return null;
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort());
            if (!useFrench && value.matches(".*[éáàç].*")) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_reallyFrench());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

}
