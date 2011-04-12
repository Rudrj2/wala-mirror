package com.ibm.wala.cfg.exc.intra;

import java.util.List;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.exc.ExceptionPruningAnalysis;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.MonitorUtil.IProgressMonitor;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

public class SSACFGNullPointerAnalysis implements ExceptionPruningAnalysis<SSAInstruction, ISSABasicBlock> {

  private final TypeReference[] ignoredExceptions;
  private IntraprocNullPointerAnalysis<ISSABasicBlock> intra = null;
  private final IR ir;
  private final ParameterState initialState;
  private final MethodState mState;
  
  public SSACFGNullPointerAnalysis(TypeReference[] ignoredExceptions, IR ir, ParameterState paramState, MethodState mState) {
    this.ignoredExceptions = (ignoredExceptions != null ? ignoredExceptions.clone() : null);
    this.ir = ir;
    this.initialState = (paramState == null ? ParameterState.createDefault(ir.getMethod()) : paramState);
    this.mState = (mState == null ? MethodState.DEFAULT : mState);
  }
  
  /* (non-Javadoc)
   * @see jsdg.exceptions.ExceptionPrunedCFGAnalysis#getOriginal()
   */
  public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getOriginal() {
    return ir.getControlFlowGraph();
  }

  /*
   * @see com.ibm.wala.cfg.exc.ExceptionPruningAnalysis#compute(com.ibm.wala.util.MonitorUtil.IProgressMonitor)
   */
  public int compute(IProgressMonitor progress) throws UnsoundGraphException, CancelException {
    ControlFlowGraph<SSAInstruction, ISSABasicBlock> orig = getOriginal();

    intra = new IntraprocNullPointerAnalysis<ISSABasicBlock>(ir, orig, ignoredExceptions, initialState, mState);
    intra.run(progress);

    return intra.getNumberOfDeletedEdges();
  }

  /* (non-Javadoc)
   * @see jsdg.exceptions.ExceptionPrunedCFGAnalysis#getPruned()
   */
  public ControlFlowGraph<SSAInstruction, ISSABasicBlock> getPruned() {
    if (intra == null) {
      throw new IllegalStateException("Run compute(IProgressMonitor) first.");
    }
    
    return intra.getPrunedCfg();
  }

  /* (non-Javadoc)
   * @see edu.kit.ipd.wala.ExceptionPrunedCFGAnalysis#hasExceptions()
   */
  public boolean hasExceptions() {
    if (intra == null) {
      throw new IllegalStateException("Run compute(IProgressMonitor) first.");
    }
    
    ControlFlowGraph<SSAInstruction, ISSABasicBlock> cfg = intra.getPrunedCfg();
    
    boolean hasException = false;
    for (ISSABasicBlock bb : cfg) {
      if (bb.getLastInstruction() == null) continue;
      List<ISSABasicBlock> succ = cfg.getExceptionalSuccessors(bb);
      if (succ != null && !succ.isEmpty()) {
        hasException = true;
        break;
      }
    }
    
    return hasException;
  }

  public NullPointerState getState(ISSABasicBlock bb) {
    if (intra == null) {
      throw new IllegalStateException("Run compute(IProgressMonitor) first.");
    }
    
    return intra.getState(bb);
  }
  
}