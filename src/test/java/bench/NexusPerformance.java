package bench;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.nexus.api.NexusContext;
import com.nexus.api.NexusContext.NexusContextBuilder;
import com.nexus.boot.DependencyRegistry;
import com.nexus.boot.RegistryProvider;
import com.nexus.core.annotations.Inject;
import com.nexus.core.annotations.Injectable;
import com.nexus.core.cqrs.CqrsBus;
import com.nexus.core.event.EventBus;

import dummy.CommandDummy;
import dummy.EventDummy;
import dummy.InjectableBaseLevelDummy;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

class NexusPerformance {

    @Test
    void benchmarkNexusContext() {
        //--- Context initialization ---
        generateClasses(100);
        long startContext = System.nanoTime();
        new NexusContextBuilder()
                .packagesToScan("dummy")
                .build();
        long endContext = System.nanoTime();
        long contextTimeMs = (endContext - startContext) / 1_000_000;
        System.out.println("Context initialization time: " + contextTimeMs + "ms");

        //Expected: ~141ms
        assertTrue(contextTimeMs < 200, "Context initialization too slow: " + contextTimeMs);
    }

    @Test
    void benchmarkDependencyResolution() {
        //--- Context initialization ---
        new NexusContextBuilder()
                .packagesToScan("dummy")
                .build();

        //Registry initiation
        DependencyRegistry di = RegistryProvider.getRegistry(DependencyRegistry.class);
        
        long startDep = System.nanoTime();
        di.get(InjectableBaseLevelDummy.class);
        long endDep = System.nanoTime();
        double depTimePerInjection = (endDep - startDep) / 100.0 / 1_000_000.0; // ms
        System.out.println("Dependency resolution per injection: " + depTimePerInjection + "ms");

        assertTrue(depTimePerInjection < 0.5, "Dependency resolution too slow");
    }

    @Test
    void benchmarkCqrsSending() {
        NexusContext ctx = new NexusContextBuilder()
                .packagesToScan("dummy")
                .build();
        CqrsBus cqrsBus = ctx.getCqrsBus();
        long startCmd = System.nanoTime();
        cqrsBus.send(CommandDummy.class, "test");
        long endCmd = System.nanoTime();
        double cmdOverheadMs = (endCmd - startCmd) / 1_000_000.0;
        System.out.println("Command execution overhead: " + cmdOverheadMs + "ms");
        assertTrue(cmdOverheadMs < 0.4, "Command execution too slow: " + cmdOverheadMs);

    }

    @Test
    void benchmarkEventSending() {
        NexusContext ctx = new NexusContextBuilder()
                .packagesToScan("dummy")
                .build();
        EventBus eventBus = ctx.getEventBus();
        long startEvent = System.nanoTime();
        eventBus.publish(EventDummy.class, new EventDummy());
        long endEvent = System.nanoTime();
        double eventMsPerHandler = (endEvent - startEvent) / 1_000_000.0;
        System.out.println("Event publishing per handler: " + eventMsPerHandler + "ms");
        assertTrue(eventMsPerHandler < 0.9, "Event publishing too slow: " + eventMsPerHandler);
    }


    public static List<Class<?>> generateClasses(int count) {
        List<Class<?>> classes = new ArrayList<>();
        Class<?> previous = null;
        
        for (int i = 0; i < count; i++) {
            String className = "dummy.InjectableLevel" + i + "Dummy";
            Class<?> current;
            Builder<Object> bb = new ByteBuddy()
                    .subclass(Object.class)
                    .name(className)
                    .annotateType(
                        AnnotationDescription.Builder.ofType(Injectable.class)
                            .define("level", i)
                            .build()
                    );
            if (i == 0) {
                // --- base case: without dependencies ---
                current = getBaseClassConstructed(bb);
            } else {
                // --- Dependency classes ---
                current = getClassConstrudted(bb, previous);
            }
            classes.add(current);
            previous = current;
        }
        return classes;
    }

    private static Class<?> getBaseClassConstructed(Builder<Object> bb) {
        try {
            return bb
                    .constructor(ElementMatchers.takesArguments(0))
                    .intercept(
                        MethodCall.invoke(Object.class.getConstructor())
                    )
                    .annotateMethod(AnnotationDescription.Builder.ofType(Inject.class).build())
                    .make()
                    .load(NexusPerformance.class.getClassLoader(), 
                            ClassLoadingStrategy.Default.INJECTION
                    )
                    .getLoaded();
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            fail("The method throw an unexpected exception: " + e.getMessage());
            return null;
        }
    }

    private static Class<?> getClassConstrudted(Builder<Object> bb, Class<?> previous) {
        try {
            return bb
                    .defineField("dependency", previous, Modifier.PRIVATE)
                    .defineConstructor(Modifier.PUBLIC)
                    .withParameter(previous, "dependency")
                    .intercept(
                        MethodCall.invoke(Object.class.getConstructor())
                            .andThen(FieldAccessor.ofField("dependency").setsArgumentAt(0))
                    )
                    .annotateMethod(AnnotationDescription.Builder.ofType(Inject.class).build())
                    .defineMethod("getDependency", previous, Modifier.PUBLIC)
                    .intercept(FieldAccessor.ofField("dependency"))
                    .make()
                    .load(NexusPerformance.class.getClassLoader(), 
                            ClassLoadingStrategy.Default.INJECTION
                    )
                    .getLoaded();
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            fail("The method throw an unexpected exception: " + e.getMessage());
            return null;
        }
    }

}
