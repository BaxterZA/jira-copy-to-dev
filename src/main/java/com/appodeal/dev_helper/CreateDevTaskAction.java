package com.appodeal.dev_helper;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.*;

public class CreateDevTaskAction extends JiraWebActionSupport {

    private final IssueManager issueManager;
    private final IssueFactory issueFactory;
    private final IssueTypeManager issueTypeManager;
    private final IssueLinkManager issueLinkManager;
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;
    private final LabelManager labelManager;

    private String id;
    private String key;
    private Collection<String> teams;
    private Collection<Priority> priorities;

    private String teamName;
    private String priorityType;

    public CreateDevTaskAction(IssueManager issueManager, IssueFactory issueFactory, IssueTypeManager issueTypeManager, IssueLinkManager issueLinkManager, IssueLinkTypeManager issueLinkTypeManager, ProjectManager projectManager, ConstantsManager constantsManager, LabelManager labelManager) {
        this.issueManager = issueManager;
        this.issueFactory = issueFactory;
        this.issueTypeManager = issueTypeManager;
        this.issueLinkManager = issueLinkManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
        this.labelManager = labelManager;
    }

    @Override
    public String doDefault() throws Exception {
        try {
            Issue originalIssue = this.issueManager.getIssueObject(Long.valueOf(this.id));
            this.key = originalIssue.getKey();

            this.teams = getDevTeams().keySet();
            this.priorities = constantsManager.getPriorities();
        } catch (Exception e) {
            addErrorMessage("Error while getting Issue info");
            return ERROR;
        }
        return INPUT;
    }

    @Override
    protected String doExecute() throws Exception {
        try {
            Issue originalIssue = this.issueManager.getIssueObject(Long.valueOf(this.id));

            Team team = getDevTeams().get(teamName);

            MutableIssue issueObject = issueFactory.getIssue();
            Project project = projectManager.getProjectObjByKey(team.getProjectKey());
            issueObject.setProjectObject(project);

            for (IssueType issueType : issueTypeManager.getIssueTypes()) {
                if (issueType.getName().equalsIgnoreCase("task")) {
                    issueObject.setIssueType(issueType);
                }
            }

            if (!team.getComponent().isEmpty()) {
                for (ProjectComponent component : project.getComponents()) {
                    if (component.getName().equalsIgnoreCase(team.getComponent())) {
                        issueObject.setComponent(Collections.singletonList(component));
                    }
                }
            }

            if (priorityType != null && !priorityType.isEmpty() && !priorityType.equals("-1")) {
                log.debug("priority is not empty: " + priorityType);
                for (Priority priority : constantsManager.getPriorities()) {
                    if (priority.getId().equals(priorityType)) {
                        issueObject.setPriority(priority);
                    }
                }
            }

            issueObject.setReporter(getLoggedInUser());
            issueObject.setSummary(originalIssue.getSummary());
            issueObject.setDescription(originalIssue.getDescription());

            Issue newIssue = issueManager.createIssueObject(getLoggedInUser(), issueObject);

            labelManager.setLabels(getLoggedInUser(), newIssue.getId(), new HashSet<String>(){{add("Support");}}, false, false);

            IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkTypesByName("Cloners").iterator().next();

            issueLinkManager.createIssueLink(originalIssue.getId(), newIssue.getId(), issueLinkType.getId(), 1L, getLoggedInUser());

            return inlineRedirectToIssueWithKey(newIssue.getKey());
        } catch (Exception var11) {
            log.debug(var11.toString());
            throw new Exception(var11);
        }
    }

    private String inlineRedirectToIssueWithKey(String issueKey) {
        return super.returnCompleteWithInlineRedirect("/browse/" + issueKey);
    }

    @Override
    protected void doValidation() {
        super.doValidation();
    }

    @SuppressWarnings("unused")
    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(String id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public Collection<String> getTeams() {
        return teams;
    }

    @SuppressWarnings("unused")
    public void setTeams(Collection<String> teams) {
        this.teams = teams;
    }

    @SuppressWarnings("unused")
    public Collection<Priority> getPriorities() {
        return priorities;
    }

    @SuppressWarnings("unused")
    public void setPriorities(Collection<Priority> priorities) {
        this.priorities = priorities;
    }

    @SuppressWarnings("unused")
    public String getTeamName() {
        return teamName;
    }

    @SuppressWarnings("unused")
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @SuppressWarnings("unused")
    public String getPriorityType() {
        return priorityType;
    }

    @SuppressWarnings("unused")
    public void setPriorityType(String priorityType) {
        this.priorityType = priorityType;
    }

    @SuppressWarnings("unused")
    public String getKey() {
        return key;
    }

    @SuppressWarnings("unused")
    public void setKey(String key) {
        this.key = key;
    }

    public ApplicationUser getLoggedInUser() {
        return ComponentAccessor.getJiraAuthenticationContext().getUser();
    }


    public static Map<String, Team> getDevTeams() {
        Map<String, Team> map = new HashMap<>();
        map.put("Android", new Team("SDK", "Android"));
        map.put("iOS", new Team("SDK", "IOS"));
        map.put("Web", new Team("APD", ""));
        map.put("Plugin", new Team("SDK", "Plugins"));
        return map;
    }

}
