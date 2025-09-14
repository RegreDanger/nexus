package com.nexus.boot;

import com.nexus.util.ClassValidator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class PackagesRegistry implements Registry<ScanResult>{

    protected PackagesRegistry() {}

    @Override
    public ScanResult registry(Object... args) {
        ClassValidator.validateArgsWithContent(args, String.class, s -> !s.isEmpty());
        String[] pkgs = ClassValidator.castArray(args, new String[] {});
        return new ClassGraph().acceptPackages(pkgs).enableAllInfo().scan();
    }

}
