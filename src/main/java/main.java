import analysis.ci.PointerAnalysisTransformer;
import soot.PackManager;
import soot.Scene;
import soot.Transform;
import soot.Transformer;
import soot.options.Options;

import java.util.Arrays;

public class main {
    public static void main(String[] args) {
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Arrays.asList("target/test-classes"));
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().set_keep_line_number(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Scene.v().loadNecessaryClasses();
        String packPhaseName = "wjtp";
        String transformerPhaseName = "wjtp.pointer_analysis";
        Transformer transformer = new PointerAnalysisTransformer();
        Transform transform = new Transform(transformerPhaseName, transformer);
        PackManager.v().getPack(packPhaseName).add(transform);
        PackManager.v().getPack(packPhaseName).apply();
        PackManager.v().writeOutput();

    }

}
