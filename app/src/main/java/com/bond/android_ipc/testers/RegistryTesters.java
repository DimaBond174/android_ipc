package com.bond.android_ipc.testers;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import com.bond.android_ipc.i.ITester;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RegistryTesters {
  public static ITester getTester(String name) {
    for (ITester  t :  testers) {
      if (t.get_test_name().equals(name)) return t;
    }
    return  null;
  }

  public static final ITester[] testers = new ITester[] {
      new TestLocalService(),
      new TestAIDLService(),
      new TestContentProvider(),
      new TestViaDisk()
  };

  public static final CompletableFuture<ArrayList<String>> testers_names
      = new CompletableFuture<>().supplyAsync(new Supplier<ArrayList<String>>() {
    @Override
    public ArrayList<String> get() {
      ArrayList<String> re = new ArrayList<>(testers.length);
      for (ITester tester :  testers) {
        re.add(tester.get_test_name());
      }
      return re;
    }
  });
}
