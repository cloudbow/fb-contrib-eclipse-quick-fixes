package tests;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;

import quickfix.DeadShadowStoreResolution;
import quickfix.InsecureRandomResolution;
import quickfix.ReturnValueIgnoreResolution;
import quickfix.SerializingErrorResolution;
import quickfix.SwitchFallThroughResolution;
import quickfix.UseVarArgsResolution;
import utils.QuickFixTestPackage;
import utils.QuickFixTestPackager;

@RunWith(JUnit4.class)
@Suite.SuiteClasses( { TestContributedQuickFixes.class})
public class TestContributedQuickFixes extends TestHarness {

    @Rule
    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "This is what the sample code does.")
    public TestWatcher watcher = new TestWatcher() {
        // This test watcher allows me to debug failing tests a bit easier
        // by allowing the Eclipse instance to be navigable after failure
        @Override
        protected void failed(Throwable e, Description description) {
            System.out.println("Failed");
            // TestingUtils.waitForUiEvents(40_000);
        }

        @Override
        protected void succeeded(Description description) {
            System.out.println("Passed");
        }
    };

    @BeforeClass
    public static void setupBeforeAllTests() throws CoreException, IOException {
        loadFilesThatNeedFixing();
    }

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testCharsetIssuesResolution() throws Exception {
        needsFBContrib();
        needsFindSecurityBugs(false);
        setPriority("Medium");
        setRank(15);

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(16, 23, 25, 30, 32, 34, // CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET
                40, 44, 48, 52, 57, 61); // CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME

        packager.setExpectedBugPatterns("CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET", "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET",
                "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET", "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET",
                "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET", "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET",
                "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME", "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME",
                "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME", "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME",
                "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME", "CSI_CHAR_SET_ISSUES_USE_STANDARD_CHARSET_NAME");

        packager.setExpectedLabels(0, "Replace with StandardCharset.UTF_8");
        packager.setExpectedLabels(1, "Replace with StandardCharset.ISO_8859_1");
        packager.setExpectedLabels(2, "Replace with StandardCharset.US_ASCII");
        packager.setExpectedLabels(3, "Replace with StandardCharset.UTF_16");
        packager.setExpectedLabels(4, "Replace with StandardCharset.UTF_16LE");
        packager.setExpectedLabels(5, "Replace with StandardCharset.UTF_16BE");

        packager.setExpectedLabels(6, "Replace with StandardCharset.UTF_8.name()");
        packager.setExpectedLabels(7, "Replace with StandardCharset.UTF_16.name()");
        packager.setExpectedLabels(8, "Replace with StandardCharset.UTF_16LE.name()");
        packager.setExpectedLabels(9, "Replace with StandardCharset.UTF_16BE.name()");
        packager.setExpectedLabels(10, "Replace with StandardCharset.US_ASCII.name()");
        packager.setExpectedLabels(11, "Replace with StandardCharset.ISO_8859_1.name()");

        checkBugsAndPerformResolution(packager.asList(), "CharsetIssuesBugs.java");
    }

    @Test
    public void testUseCharacterParameterizedMethodResolution() throws Exception {
        needsFBContrib();
        setPriority("Medium");
        setRank(20);
        // this pops up when fixing the bug on line 31 (not the fixes fault, but the tests)
        setDetector("com.mebigfatguy.fbcontrib.detect.InefficientStringBuffering", false);
        QuickFixTestPackager packager = new QuickFixTestPackager();

        packager.setExpectedLines(8, 13, 19, 23, 27, 31, 35, 39);
        packager.fillExpectedBugPatterns("UCPM_USE_CHARACTER_PARAMETERIZED_METHOD");

        packager.setExpectedLabels(0, "Replace with the char equivalent method call",
                "Use StringBuilder for String concatenation");
        packager.setExpectedLabels(1, "Replace with the char equivalent method call",
                "Use StringBuilder for String concatenation");
        packager.setExpectedLabels(2, "Use StringBuilder for String concatenation"); // char equivalent won't work
        packager.setExpectedLabels(3, "Use StringBuilder for String concatenation"); // char equivalent won't work
        packager.setExpectedLabels(4, "Replace with the char equivalent method call",
                "Use StringBuilder for String concatenation");
        packager.setExpectedLabels(5, "Replace with the char equivalent method call",
                "Use StringBuilder for String concatenation");
        packager.setExpectedLabels(6, "Replace with the char equivalent method call"); // not a concatenation
        packager.setExpectedLabels(7, "Replace with the char equivalent method call"); // not a concatenation

        packager.setFixToPerform(5, 1);

        checkBugsAndPerformResolution(packager.asList(), "SingleLengthStringBugs.java");
    }

    @Test
    public void testEntrySetResolution() throws Exception {
        // No plugins needed because WMI is in FindBugsProper
        setPriority("Medium");
        setRank(18);

        QuickFixTestPackager packager = new QuickFixTestPackager();

        packager.setExpectedLines(10, 16, 23, 30);
        packager.fillExpectedBugPatterns("WMI_WRONG_MAP_ITERATOR");

        packager.fillExpectedLabels("Replace with a foreach loop using entrySet()");

        packager.setExpectedDescriptions(
                0,
                "for(Map.Entry&lt;String,Integer&gt; entry : map.entrySet()) {<br/>String key = entry.getKey();<br/>Integer tempVar = entry.getValue();<br/>...<br/>}");
        packager.setExpectedDescriptions(
                1,
                "for(Map.Entry&lt;String,Integer&gt; entry : map.entrySet()) {<br/>String key = entry.getKey();<br/>Integer i = entry.getValue();<br/>...<br/>}");
        packager.setExpectedDescriptions(
                2,
                "for(Map.Entry&lt;String,List<Integer>&gt; entry : map.entrySet()) {<br/>String key = entry.getKey();<br/>List<Integer> someVal = entry.getValue();<br/>...<br/>}");
        packager.setExpectedDescriptions(
                3,
                "for(Map.Entry&lt;String,Set<String>&gt; entry : map.entrySet()) {<br/>String key = entry.getKey();<br/>Set<String> tempVar = entry.getValue();<br/>...<br/>}");

        checkBugsAndPerformResolution(packager.asList(), "WrongMapIteratorBugs.java");

    }

    @Test
    public void testDeadShadowStoreResolution() throws Exception {
        // No plugins needed because DLS is in FindBugsProper
        setPriority("Medium");
        setRank(15);

        setDetector("edu.umd.cs.findbugs.detect.FindDeadLocalStores", true);

        QuickFixTestPackager packager = new QuickFixTestPackager();

        packager.setExpectedLines(12, 34, 39, 42);
        packager.setExpectedBugPatterns("DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD", "DLS_DEAD_LOCAL_STORE_SHADOWS_FIELD",
                "DLS_DEAD_LOCAL_STORE", "DLS_DEAD_LOCAL_STORE");
        packager.setExpectedLabels(0, "Prefix assignment to store to field");
        packager.setExpectedLabels(1, "Prefix assignment to store to field");
        packager.setExpectedLabels(2, "Prefix assignment like DeadLocalStoreBugs.this.className");
        packager.setExpectedLabels(3); // no resolutions, it doesn't apply
        packager.fillExpectedDescriptions(DeadShadowStoreResolution.DSS_DESC);
        packager.setExpectedDescriptions(3); // no descriptions either

        packager.setFixToPerform(3, QuickFixTestPackage.IGNORE_FIX);
        checkBugsAndPerformResolution(packager.asList(), "DeadLocalStoreBugs.java");
    }

    @Test
    public void testLiteralStringComparisonResolution() throws Exception {
        needsFBContrib();
        setPriority("High");
        setRank(17);

        QuickFixTestPackager packager = new QuickFixTestPackager();

        packager.setExpectedLines(6, 12, 13, 15);
        packager.fillExpectedBugPatterns("LSC_LITERAL_STRING_COMPARISON");
        packager.fillExpectedLabels("Swap string variable and string literal");

        packager.setFixToPerform(1, QuickFixTestPackage.FIXED_BY_ANOTHER_FIX); // the last fix will fix all three problems
        packager.setFixToPerform(2, QuickFixTestPackage.FIXED_BY_ANOTHER_FIX); // I ignore 1 and 2 because that integrates with the framework

        checkBugsAndPerformResolution(packager.asList(), "LiteralStringComparisonBugs.java");
    }

    @Test
    public void testInsecureRandomResolution() throws Exception {
        needsFBContrib();
        
        setDetector("com.h3xstream.findsecbugs.PredictableRandomDetector", false); //don't want duplicate reports
        setPriority("Low");
        setRank(20);

        QuickFixTestPackager packager = new QuickFixTestPackager();

        packager.setExpectedLines(6, 8);
        packager.fillExpectedBugPatterns("MDM_RANDOM_SEED");
        packager.fillExpectedLabels("Initialize with seed from SecureRandom", "Replace using a SecureRandom object");
        packager.fillExpectedDescriptions(InsecureRandomResolution.GENERATE_SEED_DESC,
                InsecureRandomResolution.SECURE_RENAME_DESC);
        packager.setFixToPerform(0, 0);
        packager.setFixToPerform(1, 1);

        checkBugsAndPerformResolution(packager.asList(), "InsecureRandomBugs.java");
    }

    @Test
    public void testNeedlessBoxingResolution() throws Exception {
        needsFBContrib();
        
        setPriority("Low");
        setRank(20);
        setDetector("edu.umd.cs.findbugs.detect.UnreadFields", false);
        setDetector("edu.umd.cs.findbugs.detect.DumbMethods", false); // some overlap with the parse
        setDetector("com.mebigfatguy.fbcontrib.detect.FinalParameters", false);
        // disables NP_NULL_PARAM_DEREF_NONVIRTUAL which happens because the rtstubs17.jar
        // defines the constants (like Boolean.False) as null
        setDetector("edu.umd.cs.findbugs.detect.FindNullDeref", false);
        setDetector("edu.umd.cs.findbugs.detect.LoadOfKnownNullValue", false);

        QuickFixTestPackager packager = new QuickFixTestPackager();

        packager.setExpectedLines(10, 18, 19, 20, 21, 22, 23, 24, 30, 31, 32, 33, 39);

        packager.setExpectedBugPatterns("NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION",
                "NAB_NEEDLESS_BOXING_PARSE", "NAB_NEEDLESS_BOXING_PARSE", "NAB_NEEDLESS_BOXING_PARSE",
                "NAB_NEEDLESS_BOXING_PARSE",
                "NAB_NEEDLESS_BOXING_PARSE", "NAB_NEEDLESS_BOXING_PARSE", "NAB_NEEDLESS_BOXING_PARSE",
                "NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION", "NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION",
                "NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION", "NAB_NEEDLESS_BOOLEAN_CONSTANT_CONVERSION",
                "NAB_NEEDLESS_BOXING_PARSE");

        packager.setExpectedLabels(0, "Replace with Boolean.TRUE");
        packager.setExpectedLabels(1, "Replace with Boolean.parseBoolean(data)");
        packager.setExpectedLabels(2, "Replace with Byte.parseByte(data)");
        packager.setExpectedLabels(3, "Replace with Short.parseShort(data)");
        packager.setExpectedLabels(4, "Replace with Integer.parseInt(data)");
        packager.setExpectedLabels(5, "Replace with Long.parseLong(data)");
        packager.setExpectedLabels(6, "Replace with Float.parseFloat(data)");
        packager.setExpectedLabels(7, "Replace with Double.parseDouble(data)");
        packager.setExpectedLabels(8, "Replace with false");
        packager.setExpectedLabels(9, "Replace with true");
        packager.setExpectedLabels(10, "Replace with Boolean.FALSE");
        packager.setExpectedLabels(11, "Replace with Boolean.TRUE");
        packager.setExpectedLabels(12, "Replace with Integer.parseInt(num)");

        checkBugsAndPerformResolution(packager.asList(), "NeedlessBoxingBugs.java");
    }

    @Test
    public void testBigDecimalConstructorResolution() throws Exception {
        // No plugins needed because DMI is in FindBugsProper
        
        setDetector("com.mebigfatguy.fbcontrib.detect.SillynessPotPourri", false); // these have duplicate bugs
        setRank(10);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(7, 11);

        packager.fillExpectedBugPatterns("DMI_BIGDECIMAL_CONSTRUCTED_FROM_DOUBLE");

        packager.setExpectedLabels(0, "Replace with BigDecimal.valueOf(1.23456)",
                "Replace with new BigDecimal(\"1.23456\")");
        packager.setExpectedLabels(1, "Replace with BigDecimal.valueOf(1.234567)",
                "Replace with new BigDecimal(\"1.234567\")");

        packager.setFixToPerform(0, 0);
        packager.setFixToPerform(1, 1);

        checkBugsAndPerformResolution(packager.asList(), "BigDecimalStringBugs.java");
    }

    @Test
    public void testReturnValueIgnoreResolution() throws Exception {
        needsFindSecurityBugs(false);
        // No plugins needed because RV is in FindBugsProper

        setRank(19);
        setPriority("Low");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(12, 17, 25, 30);

        packager.setExpectedBugPatterns("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
                "RV_RETURN_VALUE_IGNORED", "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE");

        String ifString = ReturnValueIgnoreResolution.descriptionForWrapIf.replace("YYY", "file.createNewFile()");
        String ifNotString = ReturnValueIgnoreResolution.descriptionForNegatedWrapIf
                .replace("YYY", "file.createNewFile()");

        packager.setExpectedLabels(0, "Replace with if (file.createNewFile()) {}",
                "Replace with if (!file.createNewFile()) {}", "Store result to a local");
        packager.setExpectedDescriptions(0, ifString, ifNotString, ReturnValueIgnoreResolution.descriptionForNewLocal);

        ifString = ReturnValueIgnoreResolution.descriptionForWrapIf.replace("YYY", "file.delete()");
        ifNotString = ReturnValueIgnoreResolution.descriptionForNegatedWrapIf
                .replace("YYY", "file.delete()");

        packager.setExpectedLabels(1, "Replace with if (file.delete()) {}",
                "Replace with if (!file.delete()) {}", "Store result to a local");
        packager.setExpectedDescriptions(1, ifString, ifNotString, ReturnValueIgnoreResolution.descriptionForNewLocal);

        packager.setExpectedLabels(2, "Store result to a local", "Store result back to self");
        packager.setExpectedDescriptions(2, ReturnValueIgnoreResolution.descriptionForNewLocal,
                ReturnValueIgnoreResolution.descriptionForStoreToSelf);
        packager.setExpectedLabels(3, "Store result to a local");
        packager.setExpectedDescriptions(3, ReturnValueIgnoreResolution.descriptionForNewLocal);

        packager.setFixToPerform(0, 1);
        packager.setFixToPerform(1, 0);
        packager.setFixToPerform(2, 1);

        checkBugsAndPerformResolution(packager.asList(), "ReturnValueIgnoredBugs.java");
    }

    @Test
    public void testArraysToStringResolution() throws Exception {
        // No plugins needed because DMI is in FindBugsProper
        setRank(10);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(7, 11, 11, 18);

        packager.fillExpectedBugPatterns("DMI_INVOKING_TOSTRING_ON_ARRAY");
        packager.fillExpectedLabels("Wrap array with Arrays.toString()");

        packager.setFixToPerform(1, QuickFixTestPackage.FIXED_BY_ANOTHER_FIX); // we'll have a 2 for one fix on line 20

        checkBugsAndPerformResolution(packager.asList(), "ArraysToStringBugs.java");
    }

    @Test
    public void testSQLOffByOneResolution() throws Exception {
        // No plugins needed because SQL is in FindBugsProper
        setRank(1);
        setPriority("High");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(10, 14, 19, 28);

        packager.fillExpectedBugPatterns("SQL_BAD_RESULTSET_ACCESS");
        packager.fillExpectedLabels("Change this index to 1 instead of 0",
                "Increment this and all similar indicies in this block by 1");

        packager.setFixToPerform(2, 1);
        packager.setFixToPerform(3, 1);

        checkBugsAndPerformResolution(packager.asList(), "SQLOffByOneBugs.java");
    }

    @Test
    public void testIsNANResolution() throws Exception {
        needsFBContrib();
        
        setRank(10);
        setPriority("Medium");

        // disables FE_TEST_IF_EQUAL_TO_NOT_A_NUMBER, which is a dup
        setDetector("edu.umd.cs.findbugs.detect.FindFloatEquality", false);
        setDetector("com.mebigfatguy.fbcontrib.detect.SillynessPotPourri", true);

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(5, 11, 17, 23, 29);

        packager.fillExpectedBugPatterns("SPP_USE_ISNAN");
        packager.setExpectedLabels(0, "Replace with a call to !Float.isNaN(f)");
        packager.setExpectedLabels(1, "Replace with a call to Float.isNaN(f)");
        packager.setExpectedLabels(2, "Replace with d.isNaN()");
        packager.setExpectedLabels(3, "Replace with a call to !Double.isNaN(d)");
        packager.setExpectedLabels(4, "Replace with !doub.isNaN()");

        checkBugsAndPerformResolution(packager.asList(), "IsNANBugs.java");
    }

    @Test
    public void testCopyOverridenMethodResolution() throws Exception {
        needsFBContrib();
        
        setRank(17);
        setPriority("Medium");
        setDetector("com.mebigfatguy.fbcontrib.detect.OverlyPermissiveMethod", false);
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(7, 16, 22);

        packager.fillExpectedBugPatterns("COM_COPIED_OVERRIDDEN_METHOD");
        packager.fillExpectedLabels("Delete this method");

        checkBugsAndPerformResolution(packager.asList(), "CopyOverriddenMethodBugs.java");
    }

    @Test
    public void testShouldBeTransientResolution() throws Exception {
        // No plugins needed because SE is in FindBugsProper
        
        setRank(14);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(11, 15);

        packager.fillExpectedBugPatterns("SE_BAD_FIELD");
        packager.fillExpectedLabels("Add the transient keyword");
        packager.fillExpectedDescriptions(SerializingErrorResolution.SE_DESCRIPTION);

        checkBugsAndPerformResolution(packager.asList(), "SerializingBugs.java");
    }

    @Test
    public void testInefficiantToArrayResolution() throws Exception {
        // No plugins needed because ITA is in FindBugsProper
        
        setRank(20);
        setPriority("Low");
        setDetector("com.mebigfatguy.fbcontrib.detect.FinalParameters", false);
        setDetector("edu.umd.cs.findbugs.detect.InefficientToArray", true);
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(14, 21);

        packager.fillExpectedBugPatterns("ITA_INEFFICIENT_TO_ARRAY");
        packager.setExpectedLabels(0, "Replace with toArray(new String[names.size()])");
        packager.setExpectedLabels(1, "Replace with toArray(new Integer[this.someInts.size()])");

        checkBugsAndPerformResolution(packager.asList(), "InefficiantArrayBugs.java");
    }

    @Test
    public void testUnnecessaryStoreResolution() throws Exception {
        needsFBContrib();
        
        setRank(17);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(20, 26, 38, 54);

        packager.fillExpectedBugPatterns("USBR_UNNECESSARY_STORE_BEFORE_RETURN");
        packager.fillExpectedLabels("Remove redundant store and local variable");

        checkBugsAndPerformResolution(packager.asList(), "UnnecessaryStoreBeforeReturnBugs.java");
    }

    @Test
    public void testChangeEnumEqualsResolution() throws Exception {
        needsFBContrib();
        
        setRank(10);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(11, 15, 16);

        packager.fillExpectedBugPatterns("SPP_EQUALS_ON_ENUM");
        packager.fillExpectedLabels("Replace method call with ==");

        packager.setFixToPerform(1, QuickFixTestPackage.FIXED_BY_ANOTHER_FIX);

        checkBugsAndPerformResolution(packager.asList(), "EqualsOnEnumBugs.java");
    }

    @Test
    public void testAddDefaultCaseResolution() throws Exception {
        // No plugins needed because SF is in FindBugsProper
        
        setRank(19);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(7);

        packager.fillExpectedBugPatterns("SF_SWITCH_NO_DEFAULT");
        packager.fillExpectedLabels("Add a blank default case");

        checkBugsAndPerformResolution(packager.asList(), "NeedsDefaultCaseBugs.java");
    }

    @Test
    public void testSwitchFallThroughResolution() throws Exception {
        // No plugins needed because SF is in FindBugsProper
        
        setRank(5);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(16, 26, 43, 56);

        packager.fillExpectedBugPatterns("SF_DEAD_STORE_DUE_TO_SWITCH_FALLTHROUGH");
        packager.setExpectedLabels(0, "Add a break after store");
        packager.setExpectedLabels(1, "Add a break after store", "Return field thing before fallthrough");
        packager.setExpectedLabels(2, "Add a break after store");
        packager.setExpectedLabels(3, "Add a break after store");

        packager.fillExpectedDescriptions(SwitchFallThroughResolution.BREAK_DESCRIPTION);
        packager.setExpectedDescriptions(1, SwitchFallThroughResolution.BREAK_DESCRIPTION,
                SwitchFallThroughResolution.RETURN_FIELD.replace("YYY", "thing"));

        packager.setFixToPerform(1, 1);

        checkBugsAndPerformResolution(packager.asList(), "SwitchDeadStoreBugs.java");
    }

    @Test
    public void testUseEnumCollectionsResolution() throws Exception {
        needsFBContrib();
        
        setRank(18);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(25, 29, 34, 41);

        packager.fillExpectedBugPatterns("UEC_USE_ENUM_COLLECTIONS");
        packager.setExpectedLabels(0, "Declare badMap to be an EnumMap");
        packager.setExpectedLabels(1, "Declare badSet to be an EnumSet");
        packager.setExpectedLabels(2, "Declare badLocalSet to be an EnumSet");
        packager.setExpectedLabels(3, "Declare badLocalMap to be an EnumMap");

        checkBugsAndPerformResolution(packager.asList(), "UseEnumCollectionBugs.java");
    }

    @Test
    public void testIsEmptyResolution() throws Exception {
        needsFBContrib();
        
        setRank(17);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(11, 15, 20, 21);

        packager.fillExpectedBugPatterns("SPP_USE_ISEMPTY");
        packager.fillExpectedLabels("Replace with a call to isEmpty()");

        packager.setFixToPerform(2, QuickFixTestPackage.FIXED_BY_ANOTHER_FIX); // it's fixed with the marker on line 21

        checkBugsAndPerformResolution(packager.asList(), "IsEmptyBugs.java");
    }

    @Test
    public void testOverlyConcreteParametersResolution() throws Exception {
        needsFBContrib();
        
        setRank(17);
        setPriority("Medium");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(26, 41, 51);

        packager.fillExpectedBugPatterns("OCP_OVERLY_CONCRETE_PARAMETER");
        packager.fillExpectedLabels("Make parameter more abstract");

        checkBugsAndPerformResolution(packager.asList(), "OverlyConcreteBugs.java");
    }
    
    @Test
    public void testConvertingStringLiteralsResolution() throws Exception {
        needsFBContrib();
        
        setRank(10);
        setPriority("Low");

        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(7, 8, 11, 16, 20, 20, 24, 28, 32, 32);

        packager.fillExpectedBugPatterns("SPP_CONVERSION_OF_STRING_LITERAL");
        packager.fillExpectedLabels("Apply extraneous methods to string literal");
        //at 4 and 9  FindBugs puts another marker because there is both a medium and low version
        // So, at 4, we'll just ignore it (because the locale is static), and at 8, it will have no solutions
        packager.setFixToPerform(4, QuickFixTestPackage.FIXED_BY_ANOTHER_FIX);
        packager.setFixToPerform(6, QuickFixTestPackage.IGNORE_FIX);
        packager.setFixToPerform(9, QuickFixTestPackage.IGNORE_FIX);
        
        packager.setExpectedLabels(6);

        checkBugsAndPerformResolution(packager.asList(), "StringLiteralBugs.java");
    }
    
    @Test
    public void testUseVarArgsResolution() throws Exception {
        needsFBContrib();
        
        setRank(20);
        setPriority("Low");

        setDetector("com.mebigfatguy.fbcontrib.detect.FinalParameters", false);
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(5, 9);

        packager.fillExpectedBugPatterns("UVA_USE_VAR_ARGS");
        packager.fillExpectedLabels("Change last parameter to use varargs (...)");
        packager.fillExpectedDescriptions(UseVarArgsResolution.DESCRIPTION);
    
        checkBugsAndPerformResolution(packager.asList(), "VarArgsBugs.java");
    }
    
    @Test
    public void testLoggerOdditiesResolution() throws Exception {
        needsFBContrib();
        
        setRank(10);
        setPriority("Medium");
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(8, 10, 12);

        packager.fillExpectedBugPatterns("LO_SUSPECT_LOG_CLASS");
        packager.fillExpectedLabels("Create logger for class Log4jBugs instead");
        
        checkBugsAndPerformResolution(packager.asList(), "Log4jBugs.java");
    }
    
    @Test
    public void testHttpResourceResetResolution() throws Exception {
        needsFBContrib();
        
        setRank(10);
        setPriority("Medium");
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(15, 29, 49, 65, 93);

        packager.fillExpectedBugPatterns("HCP_HTTP_REQUEST_RESOURCES_NOT_FREED_LOCAL");
        packager.setExpectedLabels(0, "Add finally block to release connections of httpGet",
                "Add call to httpGet.releaseConnection() after catch block");
        packager.setExpectedLabels(1, "Add finally block to release connections of httpGet",
                "Add call to httpGet.releaseConnection() after catch block");
        packager.setExpectedLabels(2, "Add call to httpGet.releaseConnection() to finally block",
                "Add call to httpGet.releaseConnection() after finally block");
        packager.setExpectedLabels(3, "Add call to httpGet.releaseConnection() to finally block",
                "Add call to httpGet.releaseConnection() after finally block");
        packager.setExpectedLabels(4);
        
        packager.setFixToPerform(1, 1);
        packager.setFixToPerform(3, 1);
        packager.setFixToPerform(4, QuickFixTestPackage.IGNORE_FIX);
        
        checkBugsAndPerformResolution(packager.asList(), "HttpClientBugs.java");
    }
    
    
    @Test
    public void testFormatStringResolution() throws Exception {
        // No plugins needed because VA is in FindBugsProper
        setRank(20);
        setPriority("Medium");
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(8, 9, 10, 11, 13);

        packager.setExpectedBugPatterns("VA_FORMAT_STRING_BAD_CONVERSION_FROM_ARRAY",
                "VA_FORMAT_STRING_BAD_CONVERSION_TO_BOOLEAN",
                "VA_FORMAT_STRING_BAD_CONVERSION",
                "VA_FORMAT_STRING_BAD_CONVERSION",
                "VA_FORMAT_STRING_USES_NEWLINE" );
        
        packager.setExpectedLabels(0, "Wrap array with Arrays.toString()");
        packager.setExpectedLabels(1, "Replace bad format specifier with %s");
        packager.setExpectedLabels(2, "Replace bad format specifier with %s");
        packager.setExpectedLabels(3, "Replace bad format specifier with %s");
        packager.setExpectedLabels(4, "Replace \\n with %n");
        
        checkBugsAndPerformResolution(packager.asList(), "FormatStringBugs.java");
    }
    
    @Test
    public void testEmptyAbstractResolution() throws Exception {
        needsFBContrib();
        
        setRank(19);
        setPriority("Medium");
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(11);
        
        packager.setExpectedBugPatterns("ACEM_ABSTRACT_CLASS_EMPTY_METHODS");
        packager.setExpectedLabels(0, "Make method abstract");
        
        checkBugsAndPerformResolution(packager.asList(), "EmptyMethodsBugs.java");
    }
    
    @Test
    public void testFloatingCompareToResolution() throws Exception {
        // No plugins needed because CO is in FindBugsProper
        
        setRank(17);
        setPriority("Medium");
        setDetector("edu.umd.cs.findbugs.detect.FindHEmismatch", false);
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(15, 29, 44);
        
        packager.fillExpectedBugPatterns("CO_COMPARETO_INCORRECT_FLOATING");
        packager.setExpectedLabels(0, "Replace with Float.compare(d1, d2)");
        packager.setExpectedLabels(1, "Replace with Float.compare(d, o.d)");
        packager.setExpectedLabels(2, "Replace with Double.compare(d2, d1)");
        
        checkBugsAndPerformResolution(packager.asList(), "FloatingCompareToBugs.java");
    }    
     
    @Test
    public void testUseAssertEqualsResolution() throws Exception {
        needsFBContrib();
        
        setRank(8);
        setPriority("Medium");
        
        QuickFixTestPackager packager = new QuickFixTestPackager();
        packager.setExpectedLines(15, 16, 21, 22);
        packager.fillExpectedBugPatterns("JAO_JUNIT_ASSERTION_ODDITIES_USE_ASSERT_EQUALS");
        packager.setExpectedLabels(0, "Replace with assertEquals(2, list.size())");
        packager.setExpectedLabels(1, "Replace with assertEquals(2, list.size())");
        packager.setExpectedLabels(2, "Replace with assertEquals(s, s2)");
        packager.setExpectedLabels(3, "Replace with assertEquals(s.length(), s2.length())");

        checkBugsAndPerformResolution(packager.asList(), "TestUseAssertEqualsBugs.java");
        
    }

}
