package com.googlecode.t7mp.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.googlecode.t7mp.util.FileUtil;

public final class ModifiedFileTimerTask extends TimerTask {

    private static final String DEF_STATIC = "src/main/webapp/";
    private static final String DEF_CLASSES = "target/classes/";
    private long lastrun = System.currentTimeMillis();
    private final File rootDirectory;
    private final File webappDirectory;
    private final List<String> suffixe;

    public ModifiedFileTimerTask(File rootDirectory, File webappDirectory, List<String> suffixe) {
        this.rootDirectory = rootDirectory;
        this.webappDirectory = webappDirectory;
        this.suffixe = suffixe;
    }

    @Override
    public void run() {
        long timeStamp = lastrun;
        lastrun = System.currentTimeMillis();
        Set<File> fileSet = FileUtil.getAllFiles(rootDirectory);
        Collection<File> changedFiles = Collections2.filter(fileSet,
                Predicates.and(new ModifiedFilePredicate(timeStamp), new FileSuffixPredicate(suffixe)));
        //        Collection<File> changedFiles = Lists.newArrayList();
        for (File file : changedFiles) {
            String absolutePath = file.getAbsolutePath();
            String def = getResourceDef(absolutePath);

            int endIndex = absolutePath.lastIndexOf(def);
            String copyFragment = absolutePath.substring(endIndex + def.length());
            File copyToFile = new File(webappDirectory, copyFragment);
            System.out.println("CHANGED: " + absolutePath);
            System.out.println("GOES TO : " + copyToFile.getAbsolutePath());
            try {
                FileUtils.copyFile(file, copyToFile);
                FileUtils.touch(copyToFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("-----------END SCAN-------------");
    }

    private String getResourceDef(String absolutePath) {
        if (absolutePath.lastIndexOf(DEF_STATIC) != -1) {
            return DEF_STATIC;
        } else {
            return DEF_CLASSES;
        }
    }
}