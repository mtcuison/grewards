
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
public class EvaluationSummarized {
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
    public EvaluationSummarized(GRider foApp, String fsBranchCd, boolean fbWithParent){        
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
    public Object getBranch(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oBranch.first();
        return p_oBranch.getObject(fnIndex);
    }
    
    public Object getBranch(String fsIndex) throws SQLException{
        return getBranch(getColumnIndex(p_oBranch, fsIndex));
    }
    public void setBranch(){
        p_oBranch = null;
    }
    
    public Object getOfficer(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oEmployee.first();
        return p_oEmployee.getObject(fnIndex);
    }
    
    public Object getOfficer(String fsIndex) throws SQLException{
        return getOfficer(getColumnIndex(p_oEmployee, fsIndex));
    }
    public void setOfficer(){
        p_oEmployee = null;
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
   
    public boolean OpenRecord(String fsValue) throws SQLException{
         
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
      if(!fsValue.isEmpty()){
           lsCondition =  lsCondition + " AND a.dTransact LIKE " +SQLUtil.toSQL(fsValue + "%");
        }
        if(p_oBranch != null){
            lsCondition = lsCondition + " AND a.sBranchCD LIKE " + SQLUtil.toSQL(getBranch("sBranchCd") + "%");
        }
        if(p_oEmployee != null){
            lsCondition = lsCondition + " AND c.sEmployID LIKE " + SQLUtil.toSQL(getOfficer("sEmployID") + "%");
        }
        lsSQL = lsSQL + lsCondition + " GROUP BY a.sBranchCD ORDER BY a.dTransact DESC";

        
        System.out.println(lsSQL);
        loRS = p_oApp.executeQuery(lsSQL);
        p_oRecord = factory.createCachedRowSet();
        p_oRecord.populate(loRS);
        MiscUtil.close(loRS);

        p_nEditMode = EditMode.READY;
        return true;
    }
    public String getSQ_Branch(){
        String lsSQL = "";
        String lsStat = String.valueOf(p_nTranStat);
        
           
        lsSQL = "SELECT" + 
                    "  sBranchCd" +
                    ", sBranchNm" +
                    ", '' sPeriodxx" +
                " FROM Branch a" +
                " WHERE cRecdStat = 1";
                    
        
        return lsSQL;
    }
        public boolean OpenBranch(String fsBranch) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = MiscUtil.addCondition(getSQ_Branch(), "sBranchCd = " + SQLUtil.toSQL(fsBranch));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oBranch = factory.createCachedRowSet();
        p_oBranch.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        
        return true;
    }
    public boolean searchBranch(String fsValue, boolean fbByCode) throws SQLException{
      
        
        String lsSQL = getSQ_Branch();
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sBranchCd = " + SQLUtil.toSQL(fsValue));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "sBranchNm LIKE " + SQLUtil.toSQL(fsValue + "%"));
        
      
        JSONObject loJSON;
        
        if (p_bWithUI){
            loJSON = showFXDialog.jsonSearch(
                        p_oApp, 
                        lsSQL, 
                        fsValue, 
                        "Code»Branch Name", 
                        "sBranchCd»sBranchNm", 
                        "sBranchCd»sBranchNm", 
                        fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenBranch((String) loJSON.get("sBranchCd"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "sBranchCd = " + SQLUtil.toSQL(fsValue));   
        else {
            lsSQL = MiscUtil.addCondition(lsSQL, "sBranchNm LIKE " + SQLUtil.toSQL(fsValue + "%")); 
            lsSQL += " LIMIT 1";
        }
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){ 
            MiscUtil.close(loRS);
            p_sMessage = "No branch found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sBranchCd");
        MiscUtil.close(loRS);
        
        return OpenBranch(lsSQL);
    }
    
        public String getSQ_Officer(){
        String lsSQL = "";
        String lsStat = String.valueOf(p_nTranStat);
        
           
        lsSQL = "SELECT" + 
                    "  a.sEmployID sEmployID" +
                    ", b.sCompnyNm sCompnyNm" +
                    ", a.sPyBranch sPyBranch" +
                " FROM Employee_Master001 a" +
                " LEFT JOIN Client_Master b" +
                " ON a.sEmployID = b.sClientID " +
                " WHERE(a.sDeptIDxx IN ('021', '026', '034') " +
                " OR a.sEmpLevID = '4') ";
                    
        
        return lsSQL;
    }
        public boolean OpenOfficer(String fsEmployee) throws SQLException{
        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = MiscUtil.addCondition(getSQ_Officer(), "a.sEmployID = " + SQLUtil.toSQL(fsEmployee));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oEmployee = factory.createCachedRowSet();
        p_oEmployee.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        
        return true;
    }
    public boolean searchOfficer(String fsValue, boolean fbByCode) throws SQLException{
      
        
        String lsSQL = getSQ_Officer();
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sEmployID = " + SQLUtil.toSQL(fsValue));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%"));
        
      
        JSONObject loJSON;
        
        if (p_bWithUI){
            loJSON = showFXDialog.jsonSearch(
                        p_oApp, 
                        lsSQL, 
                        fsValue, 
                        "Employee ID»Officer Name", 
                        "sEmployID»sCompnyNm", 
                        "sEmployID»sCompnyNm", 
                        fbByCode ? 0 : 1);
            
            if (loJSON != null) 
                return OpenOfficer((String) loJSON.get("sEmployID"));
            else {
                p_sMessage = "No record selected.";
                return false;
            }
        }
        
        if (fbByCode)
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sEmployID = " + SQLUtil.toSQL(fsValue));   
        else {
            lsSQL = MiscUtil.addCondition(lsSQL, "b.sCompnyNm LIKE " + SQLUtil.toSQL(fsValue + "%")); 
            lsSQL += " LIMIT 1";
        }
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        if (!loRS.next()){ 
            MiscUtil.close(loRS);
            p_sMessage = "No Officer found for the given criteria.";
            return false;
        }
        
        lsSQL = loRS.getString("sEmployID");
        MiscUtil.close(loRS);
        
        return OpenOfficer(lsSQL);
    }
}
