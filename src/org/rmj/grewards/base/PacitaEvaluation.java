/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.rmj.grewards.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.constants.EditMode;

/**
 *
 * @author Maynard
 */
public class PacitaEvaluation {
    private final String DEBUG_MODE = "app.debug.mode";
    
   private final String MASTER_TABLE = "Pacita_Evaluation";
   private final String PACITA_RULE_TABLE = "Pacita_Rule"; 
    private final GRider p_oApp;
    private final boolean p_bWithParent;
    
    private String p_sBranchCd;
    private int p_nEditMode;

    private String p_sMessage;
    private boolean p_bWithUI = true;
    private LMasDetTrans p_oListener;
    private CachedRowSet p_oRecord;
    private CachedRowSet p_oPacita;
    private CachedRowSet p_oPacitachild;
    
    
    public PacitaEvaluation(GRider foApp, String fsBranchCd, boolean fbWithParent){
        p_oApp = foApp;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;        
                
        if (p_sBranchCd.isEmpty()) p_sBranchCd = p_oApp.getBranchCode();
                
        p_nEditMode = EditMode.UNKNOWN;
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
    public void setListener(LMasDetTrans foValue){
        p_oListener = foValue;
    }
    public Object getMaster(int fnIndex) throws SQLException{
        if (fnIndex == 0) return null;
        
        p_oRecord.first();
        return p_oRecord.getObject(fnIndex);
    }
    
    public Object getMaster(String fsIndex) throws SQLException{
        return getMaster(getColumnIndex(p_oRecord, fsIndex));
    }
    public int getItemCount() throws SQLException{
        if (p_oRecord == null) return 0;
        
        p_oRecord.last();
        return p_oRecord.getRow();
    }
    public Object getRecord(int fnRow, int fnIndex) throws SQLException{
        if (getItemCount()  == 0) return null;
        
        if (getItemCount() == 0 || fnRow > getItemCount()) return null;   
       
        p_oRecord.absolute(fnRow);
        return p_oRecord.getObject(fnIndex);
        
    }

     public Object getRecord(int fnRow, String fsIndex) throws SQLException{
        return getRecord(fnRow, getColumnIndex(p_oRecord, fsIndex));
    }
     
    public int getPacitaItemCount() throws SQLException{
        if (p_oPacita == null) return 0;
        
        p_oPacita.last();
        return p_oPacita.getRow();
    }
    
    public Object getPacita(int fnRow, int fnIndex) throws SQLException{
        if (getPacitaItemCount()  == 0) return null;
        
        if (getPacitaItemCount() == 0 || fnRow > getPacitaItemCount()) return null;   
       
        p_oPacita.absolute(fnRow);
        return p_oPacita.getObject(fnIndex);
        
    }
    
    public Object getPacita(int fnRow, String fsIndex) throws SQLException{
        return getPacita(fnRow, getColumnIndex(p_oPacita, fsIndex));
    }
         
    public int getPacitaChildItemCount() throws SQLException{
        if (p_oPacitachild == null) return 0;
        
        p_oPacitachild.last();
        return p_oPacitachild.getRow();
    }
    
    public Object getPacitaChild(int fnRow, int fnIndex) throws SQLException{
        if (getPacitaChildItemCount()  == 0) return null;
        
        if (getPacitaChildItemCount() == 0 || fnRow > getPacitaChildItemCount()) return null;   
       
        p_oPacitachild.absolute(fnRow);
        return p_oPacitachild.getObject(fnIndex);
        
    }
    
    public Object getPacitaChild(int fnRow, String fsIndex) throws SQLException{
        return getPacitaChild(fnRow, getColumnIndex(p_oPacitachild, fsIndex));
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
 
   
    public boolean LoadList(String fsTransNox) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
//        initRecord();
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
//        lsSQL = MiscUtil.addCondition(getSQ_Record(), "sTransNox= " + SQLUtil.toSQL(fsTransNox));
        loRS = p_oApp.executeQuery(getSQ_Record());
        p_oRecord = factory.createCachedRowSet();
        p_oRecord.populate(loRS);
        MiscUtil.close(loRS);
        
//        p_nEditMode = EditMode.READY;
        return true;
    }
    
    public boolean OpenRecord(String fsValue) throws SQLException{
//        p_nEditMode = EditMode.UNKNOWN;
        
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";
        
        String lsSQL;
        ResultSet loRS;
        RowSetFactory factory = RowSetProvider.newFactory();
        
        //open master
        lsSQL = MiscUtil.addCondition(getSQ_Record(), "a.sTransNox= " + SQLUtil.toSQL(fsValue));
        loRS = p_oApp.executeQuery(lsSQL);
        p_oRecord = factory.createCachedRowSet();
        p_oRecord.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        return true;
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
                " WHERE (c.sDeptIDxx IN ('021', '026', '034') OR c.sEmpLevID = '4') " +
                " ORDER BY a.dTransact DESC ";
                  System.out.println (lsSQL);
        return lsSQL;
        
    }
    
        public String getSQ_Pacita(){
        String lsSQL;
        
        lsSQL = "SELECT " +
                    "  sEvalType " +
                    ", nEntryNox " +	
                    ", sFieldNmx " +
                    ", nMaxValue " +	
                    ", cParentxx " +
                    ", sModified " +
                    ", dModified " +
                    ", dTimeStmp " +                    
                " FROM " + PACITA_RULE_TABLE  ;
        return lsSQL;
    }
        public boolean LoadRecord(String fsTransNox) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        
        p_sMessage = "";    
        String lsSQL = getSQ_Record();//+ " WHERE a.sReferNox = " + SQLUtil.toSQL(fsTransNox);
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oRecord = factory.createCachedRowSet();
        
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        p_oRecord.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        return true;
    }
        
    public boolean LoadPacita(String fsEvalType) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        String lsSQL = getSQ_Pacita()+ " WHERE sEvalType = "  + SQLUtil.toSQL(fsEvalType) +
                " AND cParentxx = 0 " +
                " UNION" +
                " SELECT " +
                  " sEvalType " + 
                  " , nEntryNox " +
                  " , sFieldNmx " +
                  " , nMaxValue " +
                  " , cParentxx " +
                  " , sModified " +
                  " , dModified " +
                  " , dTimeStmp " +
                  " FROM " + PACITA_RULE_TABLE + " WHERE " +
                  " sEvalType = " + SQLUtil.toSQL(fsEvalType) +
                  " AND cParentxx > 0 GROUP BY cParentxx  ORDER BY nEntryNox";
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oPacita = factory.createCachedRowSet();
        
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        p_oPacita.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        return true;
    }
    
        public boolean LoadPacitaChild(String fsEvalType, String fcParentxx) throws SQLException{
        if (p_oApp == null){
            p_sMessage = "Application driver is not set.";
            return false;
        }
        
        p_sMessage = "";    
        String lsSQL = getSQ_Pacita()+ " WHERE sEvalType = " + SQLUtil.toSQL(fsEvalType) +
                " AND cParentxx = " +SQLUtil.toSQL(fcParentxx) ; 
        
        ResultSet loRS = p_oApp.executeQuery(lsSQL);
        
        RowSetFactory factory = RowSetProvider.newFactory();
        p_oPacitachild = factory.createCachedRowSet();
        
        if (MiscUtil.RecordCount(loRS) == 0){
            MiscUtil.close(loRS);
            p_sMessage = "No record found for the given criteria.";
            return false;
        }
        
        p_oPacitachild.populate(loRS);
        MiscUtil.close(loRS);
        
        p_nEditMode = EditMode.READY;
        return true;
    }

        public void displayMasterFields() throws SQLException{
        if (p_nEditMode != EditMode.ADDNEW && p_nEditMode != EditMode.UPDATE) return;
        
        int lnRow = p_oRecord.getMetaData().getColumnCount();
        
        System.out.println("----------------------------------------");
        System.out.println("MASTER TABLE INFO");
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
        
        System.out.println("----------------------------------------");
        System.out.println("END: MASTER TABLE INFO");
        System.out.println("----------------------------------------");
    }
    
}
