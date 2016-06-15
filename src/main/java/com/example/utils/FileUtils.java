package com.example.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileUtils {

  public static String readFileFromResources(final String path) {
    final ClassLoader classLoader = FileUtils.class.getClassLoader();
    final File file = new File(classLoader.getResource(path).getFile());
    if (!file.exists()) {
      return null;
    }
    try {
      return new Scanner(file).useDelimiter("\\Z").next();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

}
