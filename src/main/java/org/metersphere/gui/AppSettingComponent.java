package org.metersphere.gui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.metersphere.AppSettingService;
import org.metersphere.constants.MSApiConstants;
import org.metersphere.state.*;
import org.metersphere.utils.MSApiUtil;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.metersphere.utils.CollectionUtils;

import static org.metersphere.utils.MSApiUtil.test;

@Data
public class AppSettingComponent {

    private JPanel mainSettingPanel;
    private JTextField meterSphereAddress;
    private JTextField accesskey;
    private JPasswordField secretkey;
    private JButton testCon;
    private JTabbedPane settingPanel;
    private JComboBox apiType;
    private JComboBox<MSProject> projectCB;
    private JComboBox<MSModule> moduleCB;
    private JComboBox<String> modeId;
    private JComboBox deepthCB;
    private JTextField moduleName;
    private JCheckBox javadocCheckBox;
    private JTextField contextPath;
    private JComboBox<MSProjectVersion> projectVersionCB;
    private JComboBox workspaceCB;
    private JComboBox updateVersionCB;
    private JCheckBox coverModule;
    private AppSettingService appSettingService = AppSettingService.getInstance();
    private Gson gson = new Gson();
    private Logger logger = Logger.getInstance(AppSettingComponent.class);


    private static final Map<String, Object> CONCURRENT_HASHMAP = new ConcurrentHashMap<>();

    private static final String OLD_APPSETTING_CONFIG = "oldAppSettingConfig";
    private static final String OLD_WORKSPACE_CONFIG = "oldWorksapceConfig";
    private static final String OLD_PROJECT_CONFIG = "oldProjectConfig";
    private static final String OLD_MODULE_CONFIG = "oldModuleConfig";


    {
        PropertiesComponent.getInstance().setValue(OLD_APPSETTING_CONFIG, JSON.toJSONString(appSettingService.getState()));
    }

    private static AppSettingState getOldAppSetting() {
        return JSON.parseObject(PropertiesComponent.getInstance().getValue(OLD_APPSETTING_CONFIG), AppSettingState.class);
    }

    private static boolean isCheckOldAppSetting() {
        AppSettingState oldAppSetting = getOldAppSetting();
        return StringUtils.isAnyBlank(oldAppSetting.getMeterSphereAddress(), oldAppSetting.getAccesskey(), oldAppSetting.getAccesskey());
    }

    public AppSettingComponent() {
        AppSettingState appSettingState = appSettingService.getState();
        initData(appSettingState);
        testCon.addActionListener(actionEvent -> {
            if (test(appSettingState)) {
                if (initWorkSpaceWithProject()) {
                    if (!isCheckOldAppSetting()) {
                        workspaceCB.setSelectedItem(CONCURRENT_HASHMAP.get(OLD_WORKSPACE_CONFIG));
                        projectCB.setSelectedItem(CONCURRENT_HASHMAP.get(OLD_PROJECT_CONFIG));
                        moduleCB.setSelectedItem(CONCURRENT_HASHMAP.get(OLD_MODULE_CONFIG));
                    }
                    Messages.showInfoMessage("sync success!", "Info");
                } else {
                    Messages.showInfoMessage("sync fail!", "Info");
                }
            } else {
                Messages.showInfoMessage("connect fail!", "Info");
            }
        });

        meterSphereAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appSettingState.setMeterSphereAddress(meterSphereAddress.getText());
            }
        });
        accesskey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appSettingState.setAccesskey(accesskey.getText());
            }
        });
        secretkey.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appSettingState.setSecretkey(secretkey.getText());
            }
        });

        //工作空间 action -> 项目 -> 版本
        workspaceCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                if (workspaceCB.getSelectedItem() != null) {
                    appSettingState.clear();
                    appSettingState.setWorkSpace((MSWorkSpace) workspaceCB.getSelectedItem());
                    initProject(appSettingState, ((MSWorkSpace) workspaceCB.getSelectedItem()).getId());
                }
            }
        });

        //项目 action -> 版本
        projectCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                if (projectCB.getSelectedItem() != null) {
                    appSettingState.setProject((MSProject) projectCB.getSelectedItem());
                    MSProject selectedProject = (MSProject) itemEvent.getItem();
                    initModule(selectedProject.getId());
                    if (Objects.equals(true, selectedProject.getVersionEnable())) {
                        mutationProjectVersions(selectedProject.getId());
                    }
                }
            }
        });

        projectVersionCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                if (projectVersionCB.getSelectedItem() != null)
                    appSettingState.setProjectVersion((MSProjectVersion) itemEvent.getItem());
            }
        });

        updateVersionCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                if (updateVersionCB.getSelectedItem() != null)
                    appSettingState.setUpdateVersion((MSProjectVersion) itemEvent.getItem());
            }
        });

        moduleCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                if (projectCB.getSelectedItem() != null) {
                    appSettingState.setModule((MSModule) itemEvent.getItem());
                }
            }
        });
        modeId.addActionListener(actionEvent -> {
            appSettingState.setModeId(modeId.getSelectedItem().toString());
            if (!MSApiUtil.getModeId(appSettingState.getModeId()).equalsIgnoreCase(MSApiConstants.MODE_FULLCOVERAGE)) {
                updateVersionCB.setSelectedItem(null);
                appSettingState.setUpdateVersion(null);
                updateVersionCB.setEnabled(false);
                //不覆盖的时候，覆盖路径也设置为 false
                coverModule.setSelected(false);
                coverModule.setEnabled(false);
            } else {
                updateVersionCB.setEnabled(true);
                projectVersionCB.setEnabled(true);
                //覆盖模块设置为启用
                coverModule.setEnabled(true);
                if (CollectionUtils.isNotEmpty(appSettingState.getUpdateVersionOptions())) {
                    updateVersionCB.setSelectedItem(appSettingState.getUpdateVersionOptions().get(0));
                }
            }
        });
        coverModule.addActionListener(actionEvent -> {
            appSettingState.setCoverModule(coverModule.isSelected());
        });
        deepthCB.addActionListener(actionEvent ->
                appSettingState.setDeepth(Integer.valueOf(deepthCB.getSelectedItem().toString())));
        moduleName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appSettingState.setExportModuleName(new String(moduleName.getText().trim().getBytes(StandardCharsets.UTF_8)));
            }
        });
        contextPath.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                appSettingState.setContextPath(contextPath.getText().trim());
            }
        });
        javadocCheckBox.addActionListener((actionEvent) -> appSettingState.setJavadoc(javadocCheckBox.isSelected()));
    }

    private void initData(AppSettingState appSettingState) {
        meterSphereAddress.setText(appSettingState.getMeterSphereAddress());
        accesskey.setText(appSettingState.getAccesskey());
        secretkey.setText(appSettingState.getSecretkey());
        if (appSettingState.getModeId() != null) {
            modeId.setSelectedItem(appSettingState.getModeId());
        }
        apiType.setSelectedItem(appSettingState.getApiType());
        if (StringUtils.isNotBlank(appSettingState.getExportModuleName())) {
            moduleName.setText(new String(appSettingState.getExportModuleName().getBytes(StandardCharsets.UTF_8)));
        }
        if (appSettingState.getWorkSpaceOptions() != null) {
            appSettingState.getWorkSpaceOptions().forEach(p -> workspaceCB.addItem(p));
        }
        if (appSettingState.getWorkSpace() != null) {
            workspaceCB.setSelectedItem(appSettingState.getWorkSpace());
        }

        if (appSettingState.getProjectOptions() != null) {
            appSettingState.getProjectOptions().forEach(p -> projectCB.addItem(p));
        }
        if (appSettingState.getProject() != null) {
            projectCB.setSelectedItem(appSettingState.getProject());
        }

        if (appSettingState.getModuleOptions() != null) {
            appSettingState.getModuleOptions().forEach(p -> moduleCB.addItem(p));
        }
        if (appSettingState.getModule() != null) {
            moduleCB.setSelectedItem(appSettingState.getModule());
        }

        // 版本
        if (appSettingState.getProjectVersionOptions() != null) {
            appSettingState.getProjectVersionOptions()
                    .forEach(version -> projectVersionCB.addItem(version));
        }
        if (appSettingState.getProjectVersion() != null) {
            projectVersionCB.setSelectedItem(appSettingState.getProjectVersion());
        }

        if (appSettingState.getUpdateVersionOptions() != null) {
            appSettingState.getUpdateVersionOptions()
                    .forEach(version -> updateVersionCB.addItem(version));
        }
        if (appSettingState.getUpdateVersion() != null) {
            updateVersionCB.setSelectedItem(appSettingState.getUpdateVersion());
        }

        if (appSettingState.isSupportVersion()) {
            if (!StringUtils.equalsIgnoreCase(MSApiConstants.UNCOVER, appSettingState.getModeId())) {
                updateVersionCB.setEnabled(true);
            }
            projectVersionCB.setEnabled(true);
        } else {
            projectVersionCB.setEnabled(false);
            updateVersionCB.setEnabled(false);
        }

        deepthCB.setSelectedItem(appSettingState.getDeepth().toString());

        if (StringUtils.isNotBlank(appSettingState.getContextPath())) {
            contextPath.setText(appSettingState.getContextPath().trim());
        }
        javadocCheckBox.setSelected(appSettingState.isJavadoc());
        coverModule.setSelected(appSettingState.isCoverModule());
    }

    private boolean initProject(AppSettingState appSettingState, String workspaceId) {
        //初始化项目
        JSONObject param = new JSONObject();
        param.put("userId", appSettingState.getUserId());
        if (StringUtils.isNotBlank(workspaceId)) {
            param.put("workspaceId", workspaceId);
        }
        JSONObject project = MSApiUtil.getProjectList(appSettingState, param);
        if (project != null && project.getBoolean("success")) {
            appSettingState.setProjectOptions(gson.fromJson(gson.toJson(project.getJSONArray("data")), new TypeToken<List<MSProject>>() {
            }.getType()));
        } else {
            logger.error("get project failed!");
            return false;
        }
        //设置下拉选择框
        this.projectCB.removeAllItems();
        for (MSProject s : appSettingState.getProjectOptions()) {
            this.projectCB.addItem(s);
            if (!isCheckOldAppSetting() && getOldAppSetting().getProject() != null && StringUtils.equalsIgnoreCase(s.getId(), getOldAppSetting().getProject().getId())) {
                CONCURRENT_HASHMAP.put(OLD_PROJECT_CONFIG, s);
            }
        }
        if (CollectionUtils.isNotEmpty(appSettingState.getProjectOptions())) {
            checkVersionEnable(appSettingState, appSettingState.getProjectOptions().get(0).getId());
        }
        if (CollectionUtils.isEmpty(appSettingState.getProjectOptions())) {
            this.moduleCB.removeAllItems();
            appSettingState.setModule(null);
            this.projectVersionCB.removeAllItems();
            this.updateVersionCB.removeAllItems();
            appSettingState.setProjectVersion(null);
            appSettingState.setUpdateVersion(null);
        }
        return true;
    }

    private void checkVersionEnable(AppSettingState appSettingState, String projectId) {
        boolean versionEnable = MSApiUtil.getProjectVersionEnable(appSettingState, projectId);
        appSettingState.setSupportVersion(versionEnable);
        if (!versionEnable) {
            projectVersionCB.setEnabled(false);
            updateVersionCB.setEnabled(false);
        } else {
            projectVersionCB.setEnabled(true);
            if (modeId.getSelectedItem().toString().equalsIgnoreCase(MSApiConstants.COVER)) {
                updateVersionCB.setEnabled(true);
            }
        }
    }

    /**
     * 改变项目版本下拉列表的状态值
     */
    private void mutationProjectVersions(String projectId) {
        AppSettingState appSettingState = appSettingService.getState();
        if (appSettingState == null) {
            return;
        }
        JSONObject jsonObject = MSApiUtil.listProjectVersionBy(projectId, appSettingState);
        if (jsonObject != null && jsonObject.getBoolean("success")) {
            String json = gson.toJson(jsonObject.getJSONArray("data"));
            List<MSProjectVersion> versionList = gson.fromJson(json, new TypeToken<List<MSProjectVersion>>() {
            }.getType());
            appSettingState.setProjectVersionOptions(versionList);
            appSettingState.setUpdateVersionOptions(versionList);
        }
        List<MSProjectVersion> statedVersions = appSettingState.getProjectVersionOptions();
        if (CollectionUtils.isEmpty(statedVersions)) {
            return;
        }
        //设置下拉选择框
        this.projectVersionCB.removeAllItems();
        for (MSProjectVersion version : appSettingState.getProjectVersionOptions()) {
            this.projectVersionCB.addItem(version);
        }
        this.updateVersionCB.removeAllItems();
        for (MSProjectVersion version : appSettingState.getUpdateVersionOptions()) {
            this.updateVersionCB.addItem(version);
        }
        if (modeId.getSelectedItem().toString().equalsIgnoreCase(MSApiConstants.COVER)) {
            updateVersionCB.setEnabled(true);
        } else {
            updateVersionCB.setEnabled(false);
        }
    }

    private boolean initWorkSpaceWithProject() {
        AppSettingState appSettingState = appSettingService.getState();

        JSONObject userInfo = MSApiUtil.getUserInfo(appSettingState);
        appSettingState.setUserId(userInfo.getString("data"));

        JSONObject workspaceObj = MSApiUtil.getWorkSpaceList(appSettingState, userInfo.getString("data"));
        if (workspaceObj != null && workspaceObj.getBoolean("success")) {
            appSettingState.setWorkSpaceOptions(gson.fromJson(gson.toJson(workspaceObj.getJSONArray("data")), new TypeToken<List<MSWorkSpace>>() {
            }.getType()));
        } else {
            logger.error("get workspace failed!");
            return false;
        }
        this.workspaceCB.removeAllItems();
        AppSettingState oldAppSetting = getOldAppSetting();
        for (MSWorkSpace s : appSettingState.getWorkSpaceOptions()) {
            this.workspaceCB.addItem(s);
            if (!isCheckOldAppSetting() && oldAppSetting.getWorkSpace() != null && StringUtils.equalsIgnoreCase(s.getId(), oldAppSetting.getWorkSpace().getId())) {
                CONCURRENT_HASHMAP.put(OLD_WORKSPACE_CONFIG, s);
            }
        }

        if (CollectionUtils.isNotEmpty(appSettingState.getWorkSpaceOptions())) {
            initProject(appSettingState, appSettingState.getWorkSpaceOptions().get(0).getId());
        }

        return true;
    }

    /**
     * ms 项目id
     *
     * @param msProjectId
     */
    private boolean initModule(String msProjectId) {
        AppSettingState appSettingState = appSettingService.getState();

        checkVersionEnable(appSettingState, msProjectId);
        //初始化模块
        JSONObject module = MSApiUtil.getModuleList(appSettingState, msProjectId, appSettingState.getApiType());

        if (module != null && module.getBoolean("success")) {
            appSettingState.setModuleOptions(gson.fromJson(gson.toJson(module.getJSONArray("data")), new TypeToken<List<MSModule>>() {
            }.getType()));
        } else {
            logger.error("get module failed!");
            return false;
        }

        this.moduleCB.removeAllItems();
        for (MSModule s : appSettingState.getModuleOptions()) {
            this.moduleCB.addItem(s);
            if (!isCheckOldAppSetting() && appSettingState.getModule() != null && getOldAppSetting().getModule() != null && StringUtils.equalsIgnoreCase(s.getId(), getOldAppSetting().getModule().getId())) {
                CONCURRENT_HASHMAP.put(OLD_MODULE_CONFIG, s);
            }
        }
        return true;
    }

    public JPanel getSettingPanel() {
        return this.mainSettingPanel;
    }

}
