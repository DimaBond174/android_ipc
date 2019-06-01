package com.bond.android_ipc.i;
/*
 * This is the source code of SpecNet project
 * It is licensed under MIT License.
 *
 * Copyright (c) Dmitriy Bondarenko
 * feel free to contact me: specnet.messenger@gmail.com
 */

import java.util.concurrent.CompletableFuture;

public interface ITester {
  CompletableFuture<long[]> start(ITestCase testCase);
  void  stop();
  //void  join();
  String  get_test_name();
  void  onBroAnswer(long  worktime);
}
