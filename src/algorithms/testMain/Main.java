package algorithms.testMain;


import lib.core.FinishedListener;
import lib.core.ProgressChangedListener;
import lib.core.Solver;
import lib.result.Result;
import lib.result.ResultWithPrivacy;
import lib.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
//22/0.2/domainsize6/
public class Main {
    static AtomicBoolean isSolving = new AtomicBoolean(false);

    static StringBuilder msg_ret = new StringBuilder();
    static StringBuilder msgsize_ret = new StringBuilder();
    static StringBuilder nccc_ret = new StringBuilder();
    static StringBuilder ub_ret = new StringBuilder();
    static StringBuilder ret = new StringBuilder();
    static StringBuilder runtime = new StringBuilder();
    static String[] problemDirList = new String[]{"problem/dcop/22/0.2/domainsize7/"};///We can vary the agent number or density to evaluate BIP-based algorithms on different configurations.
    static int problemInd = 0;
    static String outDir = problemDirList[problemInd];
    static String amPath = "problem/am.xml";
    static String[] agents = new String[]{

              "HSCAI_BIPK8_NCLOs",         // "HSCAIK8",
     //            "PTFB_BIPK8_NCLOs",
     //  "bnbadoptPlus_FDAC_BIPK8_NCLOs",

   //         "bnbadoptPlus_FDAC",

        //    "TreeBB_BIPk8",

    };

    static int agentIndex = -1;

    static Solver solver = new Solver();
    static int validateTime = 1;
    static FinishedListenerImpl finishedListener = new FinishedListenerImpl();
    static OnProgressChangedListenerImpl progressChangedListener = new OnProgressChangedListenerImpl();

    private static class FinishedListenerImpl implements FinishedListener {
        @Override
        public void onFinished(Result result) {
            //output
            String algo = agents[agentIndex];
            // outDir =problemDirList[problemInd];
            outDir = "../"+"实验数据/"+problemDirList[problemInd];
            System.out.println(algo + " have been done!");
            FileUtils.writeStringToFile(nccc_ret.toString(),outDir + "/" + algo +"/nccc.txt");
            FileUtils.writeStringToFile(msg_ret.toString(),outDir + "/" + algo +"/msg.txt");
            FileUtils.writeStringToFile(msgsize_ret.toString(),outDir + "/" + algo +"/msgsize.txt");
            FileUtils.writeStringToFile(ub_ret.toString(),outDir + "/" + algo +"/ub.txt");
            FileUtils.writeStringToFile(runtime.toString(),outDir + "/" + algo +"/runtime.txt");
            FileUtils.writeStringToFile(ret.toString(),outDir + "/" + algo +"/ret.txt");
//
            //clear
            ret = new StringBuilder();
            msg_ret = new StringBuilder();
            nccc_ret = new StringBuilder();
            runtime = new StringBuilder();
            ub_ret = new StringBuilder();
            msgsize_ret = new StringBuilder();

            synchronized (isSolving) {
                isSolving.set(false);
                isSolving.notify();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Monitor()).start();
    }

    private static class OnProgressChangedListenerImpl implements ProgressChangedListener {
        @Override
        public void onProgressChanged(double percentage, Result result) {
            System.out.println(percentage);
            ResultWithPrivacy resultCycle = (ResultWithPrivacy) result;
            ub_ret.append(resultCycle.getUb() + "\n");
            nccc_ret.append(resultCycle.getNcccs() + "\n");
            msg_ret.append(resultCycle.getMessageQuantity() + "  \n");
            msgsize_ret.append(resultCycle.getMessageSizeCount() + "  \n");
            runtime.append(resultCycle.getTotalTime() + " \n");


            ret.append(""+resultCycle.getUb()+"\t"+resultCycle.getMessageQuantity()+"\t"+resultCycle.getNcccs()+"\t"+resultCycle.getTotalTime()+"\t"+resultCycle.getMessageSizeCount()+"\n");
        }

        @Override
        public void interrupted(String reason) {

        }
    }

    private static class Monitor implements Runnable{
        @Override
        public void run() {
            while (true) {
                synchronized (isSolving) {
                    while (isSolving.get()) {
                        try {
                            isSolving.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    agentIndex++;
                    if (agentIndex == agents.length){
                     //   break;
                        problemInd++;
                         if (problemInd == problemDirList.length) {
                            break;
                        }
                       else {
                         //   problemInd++;
                            agentIndex = 0;
                        }
                    }

                    Date currenttime =new Date();
                    SimpleDateFormat std=new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                    System.out.println(std.format(currenttime));
                    isSolving.set(true);
                    solver.batchSolve(amPath, agents[agentIndex], problemDirList[problemInd], validateTime, finishedListener, progressChangedListener);
                }
            }
        }
    }
}
