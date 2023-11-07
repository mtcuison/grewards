
package org.rmj.grewards.report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.CommonUtils;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.EditMode;
import org.rmj.grewards.base.LMasDetTrans;
import org.json.simple.JSONObject;

/**
 *
 * @author Maynard
 */
public class EvaluationTop10 {
       private final String MASTER_TABLE = "Pacita_Evaluation";   
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    private String p_sBranchCd;
    private int p_nEditMode;
    private int p_nTranStat;
    private String p_sMessage;
    private boolean p_bWithUI = true;

    private CachedRowSet p_oRecord;
    private CachedRowSet p_oBranch;
    private CachedRowSet p_oEmployee;
    
    private LMasDetTrans p_oListener;
    public EvaluationTop10(GRider foApp, String fsBranchCd, boolean fbWithParent){        
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
                
        p_nEditMode = EditMode.UNKNOWN;
    }
     public void setTranStat(int fnValue){
        p_nTranStat = fnValue;
    }
   
    public void setListener(LMasDetTrans foValue){
        p_oListener = foValue;
    }
    
    public void setWithUI(boolean fbValue){
        p_bWithUI = fbValue;
    }
    
    public int getEditMode(){
        return p_nEditMode;
    }
    public String getMessage(){
        return p_sMessage;
    }
    private int getColumnIndex(CachedRowSet loRS, String fsValue) throws SQLException{
        int lnIndex = 0;
        int lnRow = loRS.getMetaData().getColumnCount();
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            if (fsValue.equals(loRS.getMetaData().getColumnLabel(lnCtr))){
                lnIndex = lnCtr;
                break;
            }
        }
        
        return lnIndex;
    }
    public int getItemCount() throws SQLException{
        if(p_oRecord == null) return 0;
        p_oRecord.last();
        return p_oRecord.getRow();
    }
    public Object getRecord(int fnRow, int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        if (getItemCount() == 0 || fnRow > getItemCount()) return null;
        
        p_oRecord.absolute(fnRow);
        return p_oRecord.getObject(fnIndex);
        
    }
    public Object getRecord(int fnRow, String fsIndex) throws SQLException{
        return getRecord(fnRow, getColumnIndex(p_oRecord, fsIndex));
    }
    public void displayDetFields() throws SQLException{
        int lnRow = p_oRecord.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("DETAIL TABLE INFO");
        System.out.println("----------------------------------------");
        System.out.println("Total number of columns: " + lnRow);
        System.out.println("----------------------------------------");
        
        for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
            System.out.println("Column index: " + (lnCtr) + " --> Label: " + p_oRecord.getMetaData().getColumnLabel(lnCtr));
            if (p_oRecord.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                p_oRecord.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){
                
                System.out.println("Column index: " + (lnCtr) + " --> Size: " + p_oRecord.getMetaData().getColumnDisplaySize(lnCtr));
            }
        }
        
        System.out.println("Record Count == " + getItemCount());
        System.out.println("----------------------------------------");
        System.out.println("END: DETAIL TABLE INFO");
        System.out.println("----------------------------------------");
    }
        public String getSQ_Record(){
        String lsSQL;
        
        lsSQL = "SELECT " +
                    " a.sTransNox sTransNox " +
                    " , a.dTransact dTransact " +
                    " , d.sCompnyNm sCompnyNm " +
                    " , e.sBranchNm sBranchNm " +
                    " , a.sPayloadx sPayloadx " +
                    " , a.nRatingxx nRatingxx " +
                    " , a.sUserIDxx sUserIDxx " +
                    " , a.cTranStat cTranStat " +
                    " , a.sModified sModified " +
                    " , a.dModified dModified " +
                    " , g.sAreaDesc AreaDesc " +
                    ", h.sDeptName sDeptName " +
                    ", AVG(a.nRatingxx) xRating " +
                    ", COUNT(a.sBranchCD) xBranchCount " +
                    " FROM " + MASTER_TABLE +" a " +
                   " LEFT JOIN App_User_Master b " +
                   " ON a.sUserIDxx = b.sUserIDxx " +
                   " LEFT JOIN Employee_Master001 c " +
                   " ON b.sEmployNo = c.sEmployID " +
                   " LEFT JOIN Client_Master d " +
                   " ON c.sEmployID = d.sClientID " +
                   " LEFT JOIN Branch e " +
                   " ON a.sBranchCD = e.sBranchCd " +
                   " LEFT JOIN Branch_Others f " +
                   " ON e.sBranchCd = f.sBranchCd " +
                   " LEFT JOIN Branch_Area g " +
                   " ON f.sAreaCode = g.sAreaCode " +
                   " LEFT JOIN Department h " +
                   " ON c.sDeptIDxx = h.sDeptIDxx " +
                " WHERE (c.sDeptIDxx IN ('021', '026', '034') OR c.sEmpLevID = '4') " ;
                  
        return lsSQL;
        
    }
   
    public boolean OpenRecord(String fsFromValue,String fsToValue) throws SQLException{
         
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
//        createDetail();
        p_sMessage = ""; 
        String lsSQL = getSQ_Record();
        String lsCondition ="";
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
//        
//         
      if(!fsFromValue.isEmpty() || !fsToValue.isEmpty()){
           lsCondition =  lsCondition + " AND a.dTransact Between " +SQLUtil.toSQL(fsFromValue) + " AND "+
                   SQLUtil.toSQL(fsToValue);
        }
        
        lsSQL = lsSQL + lsCondition + " GROUP BY a.sBranchCD ORDER BY xRating DESC LIMIT 10";

        
        System.out.println(lsSQL);
        loRS = p_oApp.executeQuery(lsSQL);
        p_oRecord = factory.createCachedRowSet();
        p_oRecord.populate(loRS);
        MiscUtil.close(loRS);

        p_nEditMode = EditMode.READY;
        return true;
    }
    
}
