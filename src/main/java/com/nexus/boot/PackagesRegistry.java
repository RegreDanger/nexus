package com.nexus.boot;

import com.nexus.util.ClassValidator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class PackagesRegistry implements Registry<Void>{
    
    private ScanResult sr;

    protected PackagesRegistry() {}

    @Override
    public Void registry(Object... args) {
        ClassValidator.validateArgsWithContent(args, String.class, s -> !s.isEmpty());
        String[] pkgs = ClassValidator.castArray(args, new String[] {});
        sr = new ClassGraph().acceptPackages(pkgs).enableAllInfo().scan();
        return null;
    }

    public ScanResult getScanResult() {
        if(sr == null) {
            throw new IllegalArgumentException("Registry not initialized");
        }
        return sr;
    }

}
