package cucumber.api.jelly;

import cucumber.api.jelly.internal.NbCucumberJellyLogHandler;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import org.junit.runner.Runner;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;
import static org.openide.util.Utilities.toURI;
import sun.net.www.protocol.file.FileURLConnection;


//May Want to extend a lower class to stop Cucumber Starting twice
public class Cucumber extends ParentRunner<Runner> {
    private static final Logger LOG;
    private static Cucumber.JellyRunner runner;
    
    static {
        System.setProperty("org.netbeans.MainImpl.154417", "true");
        LOG = Logger.getLogger(Cucumber.class.getName());
    }
    
    public Cucumber(Class clazz) throws InitializationError, IOException {
        super(clazz);
        runner=new Cucumber.JellyRunner(clazz);
        System.out.println(clazz.getPackage().toString());
    }

    @Override
    public void run(RunNotifier notifier){
        try {
            runner.run(notifier);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }        
    }

    @Override
    protected List<Runner> getChildren() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Description describeChild(Runner child) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This annotation can be used to give additional hints to the {@link Cucumber} runner
     * about what to run.
     * 
     * These are the Options passed down to 'Jelly Tools' to inform it about how
     * to run the tests.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public static @interface Jelly {
        public enum Level {
            OFF (java.util.logging.Level.OFF),
            SEVERE (java.util.logging.Level.SEVERE),             
            WARNING (java.util.logging.Level.WARNING),
            INFO (java.util.logging.Level.INFO),            
            CONFIG (java.util.logging.Level.CONFIG),
            FINE (java.util.logging.Level.FINE),            
            FINER (java.util.logging.Level.FINER),            
            FINEST (java.util.logging.Level.FINEST),            
            ALL (java.util.logging.Level.ALL),
;            
            
            private final java.util.logging.Level logLevel;
            
            Level(java.util.logging.Level level){
                logLevel=level;
            }
            
            java.util.logging.Level getLevel(){
                return logLevel;
            }
        }        
        
        String[] clusterRegExps() default {};
        String[] moduleRegExps()  default {};
        
        /** 
         * By default only modules on classpath of the test are enabled, 
         * the rest are just autoloads. If you need to enable more, you can
         * specify that with this method. To enable all available modules
         * in all clusters pass in <code>".*"</code>. Since 1.55 this method
         * is cummulative.
         * 
         * @return regular expression to match code name base of modules
         */
        String[] enableModules() default {};

        /**
         * Appends one or more command line arguments which will be used to 
         * start the application.  Arguments which take a parameter should
         * usually be specified as two separate strings.  Also note that this
         * annotation cannot handle arguments which must be passed directly to the 
         * JVM (such as memory settings or system properties), those should be 
         * instead specified in the <code>test.run.args</code> property (e.g.
         * in the module's <code>project.properties</code> file).
         * 
         * @return command line arguments to append; each value
         * specified here will be passed a separate argument when starting
         * the application under test.
         * @since 1.67
         */
        String[] startupArguments() default {};

        //ClassLoader parentClassLoader;
                        
        /**
         * Enables or disables userdir reuse. By default it is disabled.
         *          
         * @return true or false
         * @since 1.52
         */
        boolean reuseUserDir() default false;
        
        /** 
         * Regular expression to match clusters that shall be enabled.
         * To enable all clusters, the default, one can use <code>".*"</code>.
         * To enable ide and java clusters, it is handy to pass in 
         * <code>"ide|java"</code>.
         * <p>
         * There is no need to request presence of <code>platform</code> cluster,
         * as that is available all the time by default.
         * <p>
         * Since version 1.55 this method can be called multiple times.
         * 
         * @return regular expression to match cluster names
         */
        String clusters() default ".*";
        
        /**
         * Should the system run with GUI or without? The default behaviour
         * does not prevent any module to show UI. If <code>false</code> is 
         * used, then the whole system is instructed with <code>--nogui</code>
         * option that it shall run as much as possible in invisible mode. As
         * a result, the main window is not shown after the start, for example.
         * 
         * @return true or false
         */
        boolean gui() default false;
        
        /**
         * By default all modules on classpath are enabled (so you can link
         * with modules that you compile against), this method allows you to
         * disable this feature, which is useful if the test is known to not
         * link against any of classpath classes.
         *
         * @return pass false to ignore modules on classpath
         * @since 1.56
         */
        boolean enableClasspathModules() default false;

                
        
        /** By default all autoloads are regular modules and enabled. This
         * is maybe useful in certain situations, but does not really mimic the
         * real behaviour of the system when it is executed. Those who need
         * to as closely as possible simulate the real run, can use
         * <code>honorAutoloadEager(true)</code>.
         *
         * @return true in case autoloads shall remain autoloads and eager modules eager
         * @since 1.57
         */
        boolean honorAutoEager() default false;        
                
        /**
         * Allows to limit what modules get enabled in the system.
         * The original purpose of {@link Cucumber} was to enable
         * as much of modules as possible. This was believed to 
         * resemble the real situation in the running application the best.
         * However it turned out there
         * are situations when too much modules can break the system
         * and it is necessary to prevent loading some of them.
         * This method can achieve that.
         * <p>
         * The primary usage is for <em>Ant</em> based harness. It usually
         * contains full installation of various clusters and the application
         * picks just certain modules from that configuration. 
         * <code>hideExtraModules(true)</code> allows exclusion of these
         * modules as well.
         * <p>
         * The usefulness of this method in <em>Maven</em> based environment
         * is not that big. Usually the nbm plugin makes only necessary
         * JARs available. In combination with enableClasspathModules(false),
         * it may give you a subset of the Platform loaded in a test. In a
         * Maven-based app declaring a dependency on the whole 
         * org.netbeans.cluster:platform use the following suite expression:
         * 
         * <pre>
         * (
         *     gui=true,
         *     hideExtraModules=true,
         *     enableModules="(?!org.netbeans.modules.autoupdate|org.netbeans.modules.core.kit|org.netbeans.modules.favorites).*",
         *     enableClasspathModules=false,
         * )
         * </pre>
         * 
         * @return true if all enabled not explicitly requested modules should
         *   be hidden
         * @since 1.72
         */
        boolean hideExtraModules() default false;
        
        /**
         * Fails if there is a message sent to {@link Logger} with appropriate
         * level or higher during the test run execution.
         *
         * @return the minimal level of the message
         * @since 1.58
         */
        Level failOnMessage() default Level.OFF;        
        
        /** Fails if there is an exception reported to {@link Logger} with appropriate
         * level or higher during the test run execution.
         *
         * @return the minimal level of the message
         * @since 1.58
         */
        Level failOnException() default Level.OFF;
                
    }                
 
    static class JellyOptions {
        private final Cucumber.Jelly jellyOptions;
        private final cucumber.api.junit.Cucumber.Options cucumberOptions;
        private final ClassLoader parentClassLoader;
        private final List<Cucumber.Item> tests;
        private final Class<?> clazz;
        
        public JellyOptions(Class<?> clazz){
            jellyOptions=getJellyOptions(clazz);
            cucumberOptions=getCucumberOptions(clazz);
            tests = new ArrayList<Cucumber.Item>(0);
            parentClassLoader= ClassLoader.getSystemClassLoader().getParent();
            
            checkGlue();
            
            for (String gluePath: cucumberOptions.glue())
            {
                try {
                    for (Class<?> glueClass:getClassesForPackage(gluePath)){
                        tests.add(new Cucumber.Item(true, glueClass, null));            
                    }
                } catch (Exception e){

                }
            }
            this.clazz=clazz;
        }

        private Cucumber.Jelly getJellyOptions(Class<?> clazz) {
            return clazz.getAnnotation(Cucumber.Jelly.class);
        }                

        private cucumber.api.junit.Cucumber.Options getCucumberOptions(Class<?> clazz) {
            return clazz.getAnnotation(cucumber.api.junit.Cucumber.Options.class);
        }   

        public final void checkGlue(){
            for (String gluePath: cucumberOptions.glue())
            {
                if (gluePath.equals("cucumber.api.jelly.glue")) {
                    System.out.println("Cucumber Jelly Glue Present");
                    return;
                }
            }            
            
            System.out.println("###################################");
            System.out.println("#                                 #");
            System.out.println("# Cucumber Jelly Glue Not Present #");
            System.out.println("#                                 #");
            System.out.println("# To use it add                   #");
            System.out.println("#     cucumber.api.jelly.glue     #");
            System.out.println("# to @Options(glue)               #");            
            System.out.println("#                                 #");
            System.out.println("###################################");

        }
        
        /**
         * Private helper method
         * 
         * @param directory
         *            The directory to start with
         * @param pckgname
         *            The package name to search for. Will be needed for getting the
         *            Class object.
         * @param classes
         *            if a file isn't loaded but still is in the directory
         * @throws ClassNotFoundException
         */
        private static void checkDirectory(File directory, String pckgname,
                ArrayList<Class<?>> classes) throws ClassNotFoundException {
            File tmpDirectory;

            if (directory.exists() && directory.isDirectory()) {
                final String[] files = directory.list();

                for (final String file : files) {
                    if (file.endsWith(".class")) {
                        try {
                            classes.add(Class.forName(pckgname + '.'
                                    + file.substring(0, file.length() - 6)));
                        } catch (final NoClassDefFoundError e) {
                            // do nothing. this class hasn't been found by the
                            // loader, and we don't care.
                        }
                    } else if ((tmpDirectory = new File(directory, file))
                            .isDirectory()) {
                        checkDirectory(tmpDirectory, pckgname + "." + file, classes);
                    }
                }
            }
        }

        /**
         * Private helper method.
         * 
         * @param connection
         *            the connection to the jar
         * @param pckgname
         *            the package name to search for
         * @param classes
         *            the current ArrayList of all classes. This method will simply
         *            add new classes.
         * @throws ClassNotFoundException
         *             if a file isn't loaded but still is in the jar file
         * @throws IOException
         *             if it can't correctly read from the jar file.
         */
        private static void checkJarFile(JarURLConnection connection,
                String pckgname, ArrayList<Class<?>> classes)
                throws ClassNotFoundException, IOException {
            final JarFile jarFile = connection.getJarFile();
            final Enumeration<JarEntry> entries = jarFile.entries();
            String name;

            for (JarEntry jarEntry = null; entries.hasMoreElements()
                    && ((jarEntry = entries.nextElement()) != null);) {
                name = jarEntry.getName();

                if (name.contains(".class")) {
                    name = name.substring(0, name.length() - 6).replace('/', '.');

                    if (name.contains(pckgname)) {
                        classes.add(Class.forName(name));
                    }
                }
            }
        }

        /**
         * Attempts to list all the classes in the specified package as determined
         * by the context class loader
         * 
         * @param pckgname
         *            the package name to search
         * @return a list of classes that exist within that package
         * @throws ClassNotFoundException
         *             if something went wrong
         */
        private static ArrayList<Class<?>> getClassesForPackage(String pckgname)
                throws ClassNotFoundException {
            final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

            try {
                final ClassLoader cld = Thread.currentThread()
                        .getContextClassLoader();

                if (cld == null)
                    throw new ClassNotFoundException("Can't get class loader.");

                final Enumeration<URL> resources = cld.getResources(pckgname
                        .replace('.', '/'));
                URLConnection connection;

                for (URL url = null; resources.hasMoreElements()
                        && ((url = resources.nextElement()) != null);) {
                    try {
                        connection = url.openConnection();

                        if (connection instanceof JarURLConnection) {
                            checkJarFile((JarURLConnection) connection, pckgname,
                                    classes);
                        } else if (connection instanceof FileURLConnection) {
                            try {
                                checkDirectory(
                                        new File(URLDecoder.decode(url.getPath(),
                                                "UTF-8")), pckgname, classes);
                            } catch (final UnsupportedEncodingException ex) {
                                throw new ClassNotFoundException(
                                        pckgname
                                                + " does not appear to be a valid package (Unsupported encoding)",
                                        ex);
                            }
                        } else
                            throw new ClassNotFoundException(pckgname + " ("
                                    + url.getPath()
                                    + ") does not appear to be a valid package");
                    } catch (final IOException ioex) {
                        throw new ClassNotFoundException(
                                "IOException was thrown when trying to get all resources for "
                                        + pckgname, ioex);
                    }
                }
            } catch (final NullPointerException ex) {
                throw new ClassNotFoundException(
                        pckgname
                                + " does not appear to be a valid package (Null pointer exception)",
                        ex);
            } catch (final IOException ioex) {
                throw new ClassNotFoundException(
                        "IOException was thrown when trying to get all resources for "
                                + pckgname, ioex);
            }

            return classes;
        }        
        
        public Class<?> getCucumber(){
            return clazz;
        }
        
        public ClassLoader parentClassLoader(){
            return parentClassLoader;
        }        

        public boolean reuseUserDir(){
            return jellyOptions.reuseUserDir();
        }

        public boolean enableClasspathModules(){
            return jellyOptions.enableClasspathModules();
        }

        public boolean honorAutoEager(){
            return jellyOptions.honorAutoEager();
        }

        public List<String> moduleRegExp(){
            //TODO: Need to get this as a list
            return new ArrayList<String>();
        }

        public List<String> clusterRegExp(){
            //TODO: Need to get this as a list
            return new ArrayList<String>();
        }

        public boolean hideExtraModules(){
            return jellyOptions.hideExtraModules();
        }

        public boolean gui(){
            return jellyOptions.gui();
        }

        public List<String> startupArgs(){
            //TODO: Need to get this as a list
            return new ArrayList<String>();        
        }

        public Level failOnMessage(){
            return jellyOptions.failOnMessage().getLevel();
        }

        public Level failOnException(){       
            return jellyOptions.failOnException().getLevel();
        }
        
        public String[] glue(){
            return cucumberOptions.glue();
        }

        public List<Cucumber.Item> getTests(){
            return tests;
        }        
    }     
    
    static class JellyRunner {
        private static JellyOptions config;
        private static ClassLoader global; 
        private static File lastUserDir;
        private static int invocations;
        /**
         * JDK 7
         */
        private static Method fileToPath, pathToUri, pathsGet, pathToFile;
        private static final Set<String> pseudoModules = new HashSet<String>(Arrays.asList(
                "org.openide.util",
                "org.openide.util.lookup",
                "org.openide.modules",
                "org.netbeans.bootstrap",
                "org.openide.filesystems",
                "org.netbeans.core.startup"));
        
        public JellyRunner(Class clazz){
            config = new Cucumber.JellyOptions(clazz);
            global = Thread.currentThread().getContextClassLoader();
        }        
    
        public void run(RunNotifier result) throws Exception {
            ClassLoader before = Thread.currentThread().getContextClassLoader();
            try {
                runInRuntimeContainer(result);
            } finally {
                Thread.currentThread().setContextClassLoader(before);
            }
        }

        
        private void runInRuntimeContainer(RunNotifier result) throws Exception {
            //So we need to start things here...
            System.getProperties().remove("netbeans.dirs");
            File platform = findPlatform();
            List<URL> bootCP = new ArrayList<URL>();
            List<File> dirs = new ArrayList<File>();
            
            dirs.add(new File(platform, "lib"));
         
            File jdkHome = new File(System.getProperty("java.home"));
            if (new File(jdkHome.getParentFile(), "lib").exists()) {
                jdkHome = jdkHome.getParentFile();
            }
            dirs.add(new File(jdkHome, "lib"));

            //in case we're running code coverage, load the coverage libraries
            if (System.getProperty("code.coverage.classpath") != null) {
                dirs.add(new File(System.getProperty("code.coverage.classpath")));
            }

            for (File dir: dirs) {
                File[] jars = dir.listFiles();
                if (jars != null) {
                    for (File jar : jars) {
                        if (jar.getName().endsWith(".jar")) {
                            bootCP.add(toURI(jar).toURL());
                        }
                    }
                }
            }
            
            // loader that does not see our current classloader
            Cucumber.JellyRunner.JUnitLoader junit = new Cucumber.JellyRunner.JUnitLoader(config.parentClassLoader(), Cucumber.class.getClassLoader());
            URLClassLoader loader = new URLClassLoader(bootCP.toArray(new URL[0]), junit);
            Class<?> main = loader.loadClass("org.netbeans.Main"); // NOI18N
            Assert.assertEquals("Loaded by our classloader", loader, main.getClassLoader());
            Method m = main.getDeclaredMethod("main", String[].class); // NOI18N            

            System.setProperty("java.util.logging.config", "-");
            System.setProperty("netbeans.logger.console", "true");
            if (System.getProperty("netbeans.logger.noSystem") == null) {
                System.setProperty("netbeans.logger.noSystem", "true");
            }
            System.setProperty("netbeans.home", platform.getPath());
            System.setProperty("netbeans.full.hack", "true");

            String branding = System.getProperty("branding.token"); // NOI18N
            if (branding != null) {
                try {
                    Method setBranding = loader.loadClass("org.openide.util.NbBundle").getMethod("setBranding", String.class); // NOI18N
                    setBranding.invoke(null, branding);
                } catch (Throwable ex) {
                    if (ex instanceof InvocationTargetException) {
                        ex = ((InvocationTargetException)ex).getTargetException();
                    }
                    LOG.log(Level.WARNING, "Cannot set branding to " + branding, ex); // NOI18N
                }
            }

            File ud = new File(new File(Manager.getWorkDirPath()), "userdir" + invocations++);
            if (config.reuseUserDir()) {
                ud = lastUserDir != null ? lastUserDir : ud;
            } else {
                deleteSubFiles(ud);
            }
            lastUserDir = ud;
            ud.mkdirs();

            System.setProperty("netbeans.user", ud.getPath());

            TreeSet<String> modules = new TreeSet<String>();
            if (config.enableClasspathModules()) {
                modules.addAll(findEnabledModules(NbTestSuite.class.getClassLoader()));
            }
            modules.add("org.openide.filesystems");
            modules.add("org.openide.modules");
            modules.add("org.openide.util");
            modules.remove("org.netbeans.insane");
            modules.add("org.netbeans.core.startup");
            modules.add("org.netbeans.bootstrap");
            turnModules(ud, !config.honorAutoEager(), modules, config.moduleRegExp(), platform);
            if (config.enableClasspathModules()) {
                turnClassPathModules(ud, NbTestSuite.class.getClassLoader());
            }

            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (File f : findClusters()) {
                turnModules(ud, !config.honorAutoEager(), modules, config.moduleRegExp(), f);
                sb.append(sep);
                sb.append(f.getPath());
                sep = File.pathSeparator;
            }
            System.setProperty("netbeans.dirs", sb.toString());

            if (config.hideExtraModules()) {
                Collection<File> clusters = new LinkedHashSet<File>();
                if (config.clusterRegExp() != null) {
                    findClusters(clusters, config.clusterRegExp());
                }
                clusters.add(findPlatform());
                for (File f : clusters) {
                    disableModules(ud, f);
                }
            }

            System.setProperty("netbeans.security.nocheck", "true");
            List<Class<?>> allClasses = new ArrayList<Class<?>>(config.tests.size());
            for (Cucumber.Item item : config.tests) {
                allClasses.add(item.clazz);
            }
            preparePatches(System.getProperty("java.class.path"), System.getProperties(), allClasses.toArray(new Class<?>[0]));

            List<String> args = new ArrayList<String>();
            args.add("--nosplash");
            if (!config.gui()) {
                args.add("--nogui");
            }

            if (config.startupArgs() != null) {
                args.addAll(config.startupArgs());
            }

            Test handler = NbCucumberJellyLogHandler.registerBuffer(config.failOnMessage(), config.failOnException());
            m.invoke(null, (Object)args.toArray(new String[0]));

            Assert.assertNotNull("Global classloader is initialized", global);
            ClassLoader testLoader = global;
            try {
                testLoader.loadClass("junit.framework.Test");
                testLoader.loadClass("cucumber.api.junit.Cucumber");
                // Load our steps?
                for(Item test:config.getTests()){
                    testLoader.loadClass(test.clazz.getCanonicalName());
                }
                System.out.println("##########################");
                System.out.println("#                        #");
                System.out.println("# Running Cucumber       #");
                System.out.println("#                        #");
                System.out.println("##########################");
                               
                cucumber.api.junit.Cucumber toRun = new cucumber.api.junit.Cucumber(config.getCucumber());
//                result.fireTestFinished(Description.EMPTY);
                
                toRun.run(result);
                
                //Do the 'check' again to ensure the banner will be printed near
                //the tests
                config.checkGlue();
            } catch (ClassNotFoundException ex) {
//                result.addError(this, ex);
            } catch (NoClassDefFoundError ex) {
//                result.addError(this, ex);
            }
            if (handler != null) {
                NbCucumberJellyLogHandler.finish();
            }
            
            String n;
//            if (config.latestTestCaseClass != null) {
//                n = config.latestTestCaseClass.getName();
//            } else {
                n = "exit"; // NOI18N
//            }
            TestResult shutdownResult = new Shutdown(global, n).run();
            if (shutdownResult.failureCount() > 0) {
                final TestFailure tf = shutdownResult.failures().nextElement();
//                result.addFailure(tf.failedTest(), (AssertionFailedError)tf.thrownException());
            }
            if (shutdownResult.errorCount() > 0) {
                final TestFailure tf = shutdownResult.errors().nextElement();
//                result.addError(tf.failedTest(), tf.thrownException());
            }
        }        
        
        static File findPlatform() {
            String clusterPath = System.getProperty("cluster.path.final"); // NOI18N
            if (clusterPath != null) {
                for (String piece : tokenizePath(clusterPath)) {
                    File d = new File(piece);
                    if (d.getName().matches("platform\\d*")) {
                        return d;
                    }
                }
            }
            String allClusters = System.getProperty("all.clusters"); // #194794
            if (allClusters != null) {
                File d = new File(allClusters, "platform"); // do not bother with old numbered variants
                if (d.isDirectory()) {
                    return d;
                }
            }
            try {
                Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
                File util = toFile(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
                Assert.assertTrue("Util exists: " + util, util.exists());

                return util.getParentFile().getParentFile();
            } catch (Exception ex) {
                try {
                    File nbjunit = toFile(Cucumber.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    File harness = nbjunit.getParentFile().getParentFile();
                    Assert.assertEquals(nbjunit + " is in a folder named 'harness'", "harness", harness.getName());
                    TreeSet<File> sorted = new TreeSet<File>();
                    for (File p : harness.getParentFile().listFiles()) {
                        if (p.getName().startsWith("platform")) {
                            sorted.add(p);
                        }
                    }
                    Assert.assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                    return sorted.last();
                } catch (Exception ex2) {
                    Assert.fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
                }
                return null;
            }
        }
        
        private static String[] tokenizePath(String path) {
            List<String> l = new ArrayList<String>();
            StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
            char dosHack = '\0';
            char lastDelim = '\0';
            int delimCount = 0;
            while (tok.hasMoreTokens()) {
                String s = tok.nextToken();
                if (s.length() == 0) {
                    // Strip empty components.
                    continue;
                }
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if (c == ':' || c == ';') {
                        // Just a delimiter.
                        lastDelim = c;
                        delimCount++;
                        continue;
                    }
                }
                if (dosHack != '\0') {
                    // #50679 - "C:/something" is also accepted as DOS path
                    if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                        // We had a single letter followed by ':' now followed by \something or /something
                        s = "" + dosHack + ':' + s;
                        // and use the new token with the drive prefix...
                    } else {
                        // Something else, leave alone.
                        l.add(Character.toString(dosHack));
                        // and continue with this token too...
                    }
                    dosHack = '\0';
                }
                // Reset count of # of delimiters in a row.
                delimCount = 0;
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        // Probably a DOS drive letter. Leave it with the next component.
                        dosHack = c;
                        continue;
                    }
                }
                l.add(s);
            }
            if (dosHack != '\0') {
                //the dosHack was the last letter in the input string (not followed by the ':')
                //so obviously not a drive letter.
                //Fix for issue #57304
                l.add(Character.toString(dosHack));
            }
            return l.toArray(new String[l.size()]);
        }
        
        private static File toFile(URI u) throws IllegalArgumentException {
            if (pathsGet != null) {
                try {
                    return (File) pathToFile.invoke(pathsGet.invoke(null, u));
                } catch (Exception x) {
                    LOG.log(Level.FINE, "could not convert " + u + " to File", x);
                }
            }
            String host = u.getHost();
            if (host != null && !host.isEmpty() && "file".equals(u.getScheme())) {
                return new File("\\\\" + host + u.getPath().replace('/', '\\'));
            }
            return new File(u);
        }   

        private static final class JUnitLoader extends ClassLoader {
            private final ClassLoader junit;

            public JUnitLoader(ClassLoader parent, ClassLoader junit) {
                super(parent);
                this.junit = junit;
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (isUnit(name)) {
                    return junit.loadClass(name);
                }
                return super.findClass(name);
            }

            @Override
            public URL findResource(String name) {
                if (isUnit(name)) {
                    return junit.getResource(name);
                }
                if (name.equals("META-INF/services/java.util.logging.Handler")) { // NOI18N
                    return junit.getResource("org/netbeans/junit/internal/FakeMetaInf.txt"); // NOI18N
                }
                return super.findResource(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                if (isUnit(name)) {
                    return junit.getResources(name);
                }
                if (name.equals("META-INF/services/java.util.logging.Handler")) { // NOI18N
                    return junit.getResources("org/netbeans/junit/internal/FakeMetaInf.txt"); // NOI18N
                }
                return super.findResources(name);
            }

            private boolean isUnit(String res) {
                if (res.startsWith("junit")) {
                    return true;
                }
                if (res.startsWith("org.junit") || res.startsWith("org/junit")) {
                    return true;
                }
                //Obviously we now need Cucumber
                if (res.startsWith("cucumber.api.jelly.glue") || res.startsWith("cucumber.api.jelly.glue")) {
                    //But we want glue explicitly included!!!
                    return false;
                }                                
                if (res.startsWith("cucumber") || res.startsWith("cucumber")) {
                    return true;
                }                
                //May need hamcrest helpers
                if (res.startsWith("org.hamcrest") || res.startsWith("org/hamcrest")) {
                    return true;
                }
                if (res.startsWith("org.netbeans.junit") || res.startsWith("org/netbeans/junit")) {
                    if (res.startsWith("org.netbeans.junit.ide") || res.startsWith("org/netbeans/junit/ide")) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        }
 
        // private method for deleting every subfiles/subdirectories of a file object
        static void deleteSubFiles(File file) throws IOException {
            File files[] = file.getCanonicalFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFile(f);
                }
            } else {
                // probably do nothing - file is not a directory
            }
        }
        
        // private method for deleting a file/directory (and all its subdirectories/files)
        private static void deleteFile(File file) throws IOException {
            if (file.isDirectory() && file.equals(file.getCanonicalFile())) {
                // file is a directory - delete sub files first
                File files[] = file.listFiles();
                for (File file1 : files) {
                    deleteFile(file1);
                }
            }
            // file is a File :-)
            boolean result = file.delete();
            if (result == false ) {
                // a problem has appeared
                throw new IOException("Cannot delete file, file = "+file.getPath());
            }
        }
        
        /** Looks for all modules on classpath of given loader and builds 
         * their list from them.
         */
        static Set<String> findEnabledModules(ClassLoader loader) throws IOException {
            Set<String> cnbs = new TreeSet<String>();

            Enumeration<URL> en = loader.getResources("META-INF/MANIFEST.MF");
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                InputStream is = url.openStream();
                try {
                    String cnb = cnb(new Manifest(is));
                    if (cnb != null) {
                        cnbs.add(cnb);
                    }
                } finally {
                    is.close();
                }
            }

            return cnbs;
        }        

        private File[] findClusters() throws IOException {
            Collection<File> clusters = new LinkedHashSet<File>();
            if (config.clusterRegExp() != null) {
                findClusters(clusters, config.clusterRegExp());
            }

            if (config.enableClasspathModules()) {
                // find "cluster" from
                // k/o.n.m.a.p.N/csam/testModule/build/cluster/modules/org-example-testModule.jar
                // tested in apisupport.project
                for (String s : tokenizePath(System.getProperty("java.class.path"))) {
                    File module = new File(s);
                    File cluster = module.getParentFile().getParentFile();
                    File m = new File(new File(cluster, "config"), "Modules");
                    if (m.exists() || cluster.getName().equals("cluster")) {
                        clusters.add(cluster);
                    }
                }
            }
            return clusters.toArray(new File[0]);
        }
        
        static void findClusters(Collection<File> clusters, List<String> regExps) throws IOException {
            File plat = findPlatform().getCanonicalFile();
            String selectiveClusters = System.getProperty("cluster.path.final"); // NOI18N
            Set<File> path;
            if (selectiveClusters != null) {
                path = new TreeSet<File>();
                for (String p : tokenizePath(selectiveClusters)) {
                    File f = new File(p);
                    path.add(f.getCanonicalFile());
                }
            } else {
                File parent;
                String allClusters = System.getProperty("all.clusters"); // #194794
                if (allClusters != null) {
                    parent = new File(allClusters);
                } else {
                    parent = plat.getParentFile();
                }
                path = new TreeSet<File>(Arrays.asList(parent.listFiles()));
            }
            for (String c : regExps) {
                for (File f : path) {
                    if (f.equals(plat)) {
                        continue;
                    }
                    if (!f.getName().matches(c)) {
                        continue;
                    }
                    File m = new File(new File(f, "config"), "Modules");
                    if (m.exists()) {
                        clusters.add(f);
                    }
                }
            }
        }

        private static String cnb(Manifest m) {
            String cn = m.getMainAttributes().getValue("OpenIDE-Module");
            return cn != null ? cn.replaceFirst("/\\d+", "") : null;
        }
        
        private static void turnModules(File ud, boolean autoloads, TreeSet<String> modules, List<String> regExp, File... clusterDirs) throws IOException {
            if (regExp == null) {
                return;
            }
            File config = new File(new File(ud, "config"), "Modules");
            config.mkdirs();

            Iterator<String> it = regExp.iterator();
            for (;;) {
                if (!it.hasNext()) {
                    break;
                }
                String clusterReg = it.next();
                String moduleReg = it.next();
                Pattern modPattern = Pattern.compile(moduleReg);
                for (File c : clusterDirs) {
                    if (!c.getName().matches(clusterReg)) {
                        continue;
                    }

                    File modulesDir = new File(new File(c, "config"), "Modules");
                    File[] allModules = modulesDir.listFiles();
                    if (allModules == null) {
                        continue;
                    }
                    for (File m : allModules) {
                        String n = m.getName();
                        if (n.endsWith(".xml")) {
                            n = n.substring(0, n.length() - 4);
                        }
                        n = n.replace('-', '.');

                        String xml = asString(new FileInputStream(m), true);

                        boolean contains = modules.contains(n);
                        if (!contains && modPattern != null) {
                            contains = modPattern.matcher(n).matches();
                        }
                        if (!contains) {
                            continue;
                        }
                        enableModule(xml, autoloads, contains, new File(config, m.getName()));
                    }
                }
            }
        }

        private static final Pattern ENABLED = Pattern.compile("<param name=[\"']enabled[\"']>([^<]*)</param>", Pattern.MULTILINE);
        private static final Pattern AUTO = Pattern.compile("<param name=[\"']autoload[\"']>([^<]*)</param>", Pattern.MULTILINE);
        private static final Pattern EAGER = Pattern.compile("<param name=[\"']eager[\"']>([^<]*)</param>", Pattern.MULTILINE);        
        
        private static boolean isModuleEnabled(File config) throws IOException {
            String xml = asString(new FileInputStream(config), true);
            Matcher matcherEnabled = ENABLED.matcher(xml);
            if (matcherEnabled.find()) {
                return "true".equals(matcherEnabled.group(1));
            }
            return false;
        }

        private void disableModules(File ud, File cluster) throws IOException {
            File confDir = new File(new File(cluster, "config"), "Modules");
            for (File c : confDir.listFiles()) {
                if (!isModuleEnabled(c)) {
                    continue;
                }
                File udC = new File(new File(new File(ud, "config"), "Modules"), c.getName());
                if (!udC.exists()) {
                    File hidden = new File(udC.getParentFile(), c.getName() + "_hidden");
                    hidden.createNewFile();
                }
            }
        }

        private static void enableModule(String xml, boolean autoloads, boolean enable, File target) throws IOException {
            boolean toEnable = false;
            {
                  Matcher matcherEnabled = ENABLED.matcher(xml);
                if (matcherEnabled.find()) {
                    toEnable = "false".equals(matcherEnabled.group(1));
                }
                Matcher matcherEager = EAGER.matcher(xml);
                if (matcherEager.find()) {
                    if ("true".equals(matcherEager.group(1))) {
                        return;
                    }
                }
                if (!autoloads) {
                    Matcher matcherAuto = AUTO.matcher(xml);
                    if (matcherAuto.find()) {
                        if ("true".equals(matcherAuto.group(1))) {
                            return;
                        }
                    }
                }
                if (toEnable) {
                    assert matcherEnabled.groupCount() == 1 : "Groups: " + matcherEnabled.groupCount() + " for:\n" + xml;
                    try {
                        String out = xml.substring(0, matcherEnabled.start(1)) + (enable ? "true" : "false") + xml.substring(matcherEnabled.end(1));
                        writeModule(target, out);
                    } catch (IllegalStateException ex) {
                        throw new IOException("Unparsable:\n" + xml, ex);
                    }
                }
            }
            {
                Matcher matcherEager = AUTO.matcher(xml);
                if (matcherEager.find()) {
                    int begin = xml.indexOf("<param name=\"autoload");
                    int end = xml.indexOf("<param name=\"jar");
                    String middle = "<param name=\"autoload\">false</param>\n" + "    <param name=\"eager\">false</param>\n" + "    <param name=\"enabled\">true</param>\n" + "    ";
                    String out = xml.substring(0, begin) + middle + xml.substring(end);
                    try {
                        writeModule(target, out);
                    } catch (IllegalStateException ex) {
                        throw new IOException("Unparsable:\n" + xml, ex);
                    }
                }
            }
        }
               
        private static void writeModule(File file, String xml) throws IOException {
            String previous;
            if (file.exists()) {
                previous = asString(new FileInputStream(file), true);
                if (previous.equals(xml)) {
                    return;
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "rewrite module file: {0}", file);
                    charDump(Level.FINEST, previous);
                    LOG.finest("new----");
                    charDump(Level.FINEST, xml);
                    LOG.finest("end----");
                }
            }
            FileOutputStream os = new FileOutputStream(file);
            os.write(xml.getBytes("UTF-8"));
            os.close();
        }

        private static void charDump(Level logLevel, String text) {
            StringBuilder sb = new StringBuilder(5 * text.length());
            for (int i = 0; i < text.length(); i++) {
                if (i % 8 == 0) {
                    if (i > 0) {
                        sb.append('\n');
                    }
                } else {
                    sb.append(' ');
                }

                int ch = text.charAt(i);
                if (' ' <= ch && ch <= 'z') {
                    sb.append('\'').append((char)ch).append('\'');
                } else {
                    sb.append('x').append(two(Integer.toHexString(ch).toUpperCase()));
                }
            }
            sb.append('\n');
            LOG.log(logLevel, sb.toString());
        }

        private static String two(String s) {
            int len = s.length();
            switch (len) {
                case 0: return "00";
                case 1: return "0" + s;
                case 2: return s;
                default: return s.substring(len - 2);
            }
        }
        
        static void turnClassPathModules(File ud, ClassLoader loader) throws IOException {
            Enumeration<URL> en = loader.getResources("META-INF/MANIFEST.MF");
            while (en.hasMoreElements()) {
                URL url = en.nextElement();
                Manifest m;
                InputStream is = url.openStream();
                try {
                    m = new Manifest(is);
                } catch (IOException x) {
                    throw new IOException("parsing " + url + ": " + x, x);
                } finally {
                    is.close();
                }
                String cnb = cnb(m);
                if (cnb != null) {
                    File jar = jarFromURL(url);
                    if (jar == null) {
                        continue;
                    }
                    if (pseudoModules.contains(cnb)) {
                        // Otherwise will get DuplicateException.
                        continue;
                    }
                    String mavenCP = m.getMainAttributes().getValue("Maven-Class-Path");
                    if (mavenCP != null) {
                        // Do not use ((URLClassLoader) loader).getURLs() as this does not work for Surefire Booter.
                        jar = rewrite(jar, mavenCP.split(" "), System.getProperty("java.class.path"));
                    }
                    String xml =
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
                        "                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
                        "<module name=\"" + cnb + "\">\n" +
                        "    <param name=\"autoload\">false</param>\n" +
                        "    <param name=\"eager\">false</param>\n" +
                        "    <param name=\"enabled\">true</param>\n" +
                        "    <param name=\"jar\">" + jar + "</param>\n" +
                        "    <param name=\"reloadable\">false</param>\n" +
                        "</module>\n";
                    
                    File conf = new File(new File(ud, "config"), "Modules");
                    conf.mkdirs();
                    File f = new File(conf, cnb.replace('.', '-') + ".xml");
                    writeModule(f, xml);
                }
            }
        }        
        
        private static final Pattern MANIFEST = Pattern.compile("jar:(file:.*)!/META-INF/MANIFEST.MF", Pattern.MULTILINE);
        private static File jarFromURL(URL u) {
            Matcher m = MANIFEST.matcher(u.toExternalForm());
            if (m.matches()) {
                return toFile(URI.create(m.group(1)));
            } else {
                if (!u.getProtocol().equals("file")) {
                    throw new IllegalStateException(u.toExternalForm());
                } else {
                    return null;
                }
            }
        }
 
        private static File rewrite(File jar, String[] mavenCP, String classpath) throws IOException { // #190992
            String[] classpathEntries = tokenizePath(classpath);
            StringBuilder classPathHeader = new StringBuilder();
            for (String artifact : mavenCP) {
                String[] grpArtVers = artifact.split(":");
                String partOfPath = File.separatorChar + grpArtVers[0].replace('.', File.separatorChar) + File.separatorChar + grpArtVers[1] + File.separatorChar + grpArtVers[2] + File.separatorChar + grpArtVers[1] + '-' + grpArtVers[2];
                File dep = null;
                for (String classpathEntry : classpathEntries) {
                    if (classpathEntry.endsWith(".jar") && classpathEntry.contains(partOfPath)) {
                        dep = new File(classpathEntry);
                        break;
                    }
                }
                if (dep == null) {
                    throw new IOException("no match for " + artifact + " found in " + classpath);
                }
                File depCopy = File.createTempFile(artifact.replace(':', '-') + '-', ".jar");
                depCopy.deleteOnExit();
                copytree(dep, depCopy);
                if (classPathHeader.length() > 0) {
                    classPathHeader.append(' ');
                }
                classPathHeader.append(depCopy.getName());
            }
            String n = jar.getName();
            int dot = n.lastIndexOf('.');
            File jarCopy = File.createTempFile(n.substring(0, dot) + '-', n.substring(dot));
            jarCopy.deleteOnExit();
            InputStream is = new FileInputStream(jar);
            try {
                OutputStream os = new FileOutputStream(jarCopy);
                try {
                    JarInputStream jis = new JarInputStream(is);
                    Manifest mani = new Manifest(jis.getManifest());
                    mani.getMainAttributes().putValue("Class-Path", classPathHeader.toString());
                    JarOutputStream jos = new JarOutputStream(os, mani);
                    JarEntry entry;
                    while ((entry = jis.getNextJarEntry()) != null) {
                        if (entry.getName().matches("META-INF/.+[.]SF")) {
                            throw new IOException("cannot handle signed JARs");
                        }
                        jos.putNextEntry(entry);
                        byte[] buf = new byte[(int) entry.getSize()];
                        int read = 0;
                        while (read < buf.length) {
                            int more = jis.read(buf, read, buf.length - read);
                            if (more == -1) {
                                break;
                            }
                            read += more;
                        }
                        if (read != buf.length) {
                            throw new IOException("read wrong amount");
                        }
                        jos.write(buf);
                    }
                    jis.close();
                    jos.close();
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
            return jarCopy;
        }
 
        private static String asString(InputStream is, boolean close) throws IOException {
            StringBuilder builder = new StringBuilder();

            byte[] bytes = new byte[4096];
            try {
                for (int i; (i = is.read(bytes)) != -1;) {
                    builder.append(new String(bytes, 0, i, "UTF-8"));
                }
            } finally {
                if (close) {
                    is.close();
                }
            }
            for (;;) {
                int index = builder.indexOf("\r\n");
                if (index == -1) {
                    break;
                }
                builder.deleteCharAt(index);
            }
            return builder.toString();
        }

        static void preparePatches(String path, Properties prop, Class<?>... classes) throws URISyntaxException {
            Pattern tests = Pattern.compile(".*\\" + File.separator + "([^\\" + File.separator + "]+)\\" + File.separator + "tests\\.jar");
            System.out.println("Preparing Patches");
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String jar : tokenizePath(path)) {
                Matcher m = tests.matcher(jar);
                if (m.matches()) {
                    // in case we need it one day, let's add a switch to Configuration
                    // and choose the following line instead of netbeans.systemclassloader.patches
                    // prop.setProperty("netbeans.patches." + m.group(1).replace('-', '.'), jar);
                    sb.append(sep).append(jar);
                    sep = File.pathSeparator;
                }
            }
            Set<URL> uniqueURLs = new HashSet<URL>();
            for (Class<?> c : classes) {
                URL test = c.getProtectionDomain().getCodeSource().getLocation();
                Assert.assertNotNull("URL found for " + c, test);
                if (uniqueURLs.add(test)) {
                    String patchPath=toFile(test.toURI()).getPath();
                    System.out.println("\t"+patchPath);
                    sb.append(sep).append(patchPath);
                    sep = File.pathSeparator;
                }
            }            
            prop.setProperty("netbeans.systemclassloader.patches", sb.toString());
        }
        
        static void copytree(File from, File to) throws IOException {
            if (from.isDirectory()) {
                if (!to.mkdirs()) {
                    throw new IOException("mkdir: " + to);
                }
                for (File f : from.listFiles()) {
                    copytree(f, new File(to, f.getName()));
                }
            } else {
                InputStream is = new FileInputStream(from);
                try {
                    OutputStream os = new FileOutputStream(to);
                    try {
                        // XXX using FileChannel would be more efficient, but more complicated
                        BufferedInputStream bis = new BufferedInputStream(is);
                        BufferedOutputStream bos = new BufferedOutputStream(os);
                        int c;
                        while ((c = bis.read()) != -1) {
                            bos.write(c);
                        }
                        bos.flush();
                        bos.close();
                    } finally {
                        os.close();
                    }
                } finally {
                    is.close();
                }
            }
        }

        private static class Shutdown extends NbTestCase {
            Shutdown(ClassLoader global, String testClass) throws Exception {
                super("shuttingDown[" + testClass + "]");
                this.global = global;
            }

            @Override
            protected int timeOut() {
                return 180000; // 3 minutes for a shutdown
            }

            @Override
            protected Level logLevel() {
                return Level.FINE;
            }

            @Override
            protected String logRoot() {
                return "org.netbeans.core.NbLifecycleManager"; // NOI18N
            }
            
            private static void waitForAWT() throws InvocationTargetException, InterruptedException {
                final CountDownLatch cdl = new CountDownLatch(1);
                SwingUtilities.invokeLater(new Runnable() {
                    public @Override void run() {
                        cdl.countDown();
                    }
                });
                cdl.await(10, TimeUnit.SECONDS);
            }
            private final ClassLoader global;

            @Override
            protected void runTest() throws Throwable {
                JFrame shutDown;
                try {
                    shutDown = new JFrame("Shutting down NetBeans...");
                    shutDown.setBounds(new Rectangle(-100, -100, 50, 50));
                    shutDown.setVisible(true);
                } catch (HeadlessException ex) {
                    shutDown = null;
                }
                
                Class<?> lifeClazz = global.loadClass("org.openide.LifecycleManager"); // NOI18N
                Method getDefault = lifeClazz.getMethod("getDefault"); // NOI18N
                Method exit = lifeClazz.getMethod("exit");
                LOG.log(Level.FINE, "Closing via LifecycleManager loaded by {0}", lifeClazz.getClassLoader());
                Object life = getDefault.invoke(null);
                if (!life.getClass().getName().startsWith("org.openide.LifecycleManager")) { // NOI18N
                    System.setProperty("netbeans.close.no.exit", "true"); // NOI18N
                    System.setProperty("netbeans.close", "true"); // NOI18N
                    exit.invoke(life);
                    waitForAWT();
                    System.getProperties().remove("netbeans.close"); // NOI18N
                    System.getProperties().remove("netbeans.close.no.exit"); // NOI18N
                }
                
                if (shutDown != null) {
                    shutDown.setVisible(false);
                }
            }
        }
 
    }    
    
    private static final class Item {
        boolean isTestCase;
        Class<?> clazz;
        String[] fileNames;

        public Item(boolean isTestCase, Class<?> clazz, String[] fileNames) {
            this.isTestCase = isTestCase;
            this.clazz = clazz;
            this.fileNames = fileNames;
        }
    }
    
}