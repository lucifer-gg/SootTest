package analysis.ci;

import elem.Method;
import elem.Obj;
import elem.TaintObj;
import elem.Variable;
import soot.*;

import java.sql.Time;
import java.util.*;

public class PointerAnalysisTransformer extends SceneTransformer {

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        List<SootMethod> entries = new LinkedList<>();
        Set<SootMethod> Sources=new LinkedHashSet<>();
        Set<SootMethod> Sink=new LinkedHashSet<>();
        //先找到入口方法
        for (SootClass clazz : Scene.v().getApplicationClasses()) {
            for (SootMethod method : clazz.getMethods()) {
                if ("main".equals(method.getName())) {
                    entries.add(method);
                }
                if("getPassword".equals(method.getName())){
                    Sources.add(method);
                }
                if("check".equals(method.getName())){
                    Sink.add(method);
                }
                if("testAA".equals(method.getName())){
                    Iterator<Unit> iterator = method.retrieveActiveBody().getUnits().iterator();
                    while (iterator.hasNext()){
                        System.out.println(iterator.next().toString());
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        for (SootMethod entry : entries) {
            //然后执行分析
            PointerAnalysis pointerAnalysis = new PointerAnalysis(entry,Sources,Sink);
            pointerAnalysis.solve();

            //获取PFG图输出结果
            PointerFlowGraph PFG = pointerAnalysis.PFG;
//            System.out.println("======== PFG ========");
//            Set<Pointer> pointerSet = new LinkedHashSet<>();
//            pointerSet.addAll(PFG.varMap.values());
//            pointerSet.addAll(PFG.instanceFieldMap.values());
//            for (Pointer pointer : pointerSet) {
//                StringBuilder buff = new StringBuilder();
//                buff.append(pointer).append("\n");
//                buff.append("\t pts: ").append(pointer.getPointsToSet()).append("\n");
//                buff.append("\t edges: ").append(PFG.getSuccessorOf(pointer)).append("\n");
//                System.out.println(buff);
//            }
//            System.out.println("======== End of PFG ========\n");

            System.out.println("======分析报告===========");
            Sink.forEach(sink->{
                Method sinkMethod=new Method(sink);
                List<Variable> params = sinkMethod.getParams();
                params.forEach(param->{
                    Var paramVar = PFG.varMap.get(param);
                    PointsToSet pointsToSet = paramVar.getPointsToSet();
                    for(Obj pointerObj:pointsToSet){
                        if (pointerObj instanceof TaintObj){
                            System.out.print(pointerObj);
                            System.out.println(paramVar.getVariable().toString());
                            System.out.println("============");
                        }
                    }

                });


            });
        }
    }
}

