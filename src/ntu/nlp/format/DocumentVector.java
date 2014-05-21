package ntu.nlp.format;

import java.util.HashMap;
import java.util.Map;

public class DocumentVector {
	private int id;
	private int like;
	private Dimension [] dimensionArray;
	public DocumentVector(int id, int like , Dimension [] dimensionArray) {
		this.id = id;
		this.like = like;
		this.dimensionArray = dimensionArray;
		
		
	}
	public String toLIBSVMString(int maxDimension) {
		String libsvm = like + " ";
		boolean haveWritten = false;
		for(Dimension d : dimensionArray){
			libsvm += d.getDimension()+":"+d.getValue()+" ";
			if(d.getDimension() == maxDimension)haveWritten = true;
		}
		if(!haveWritten)libsvm += maxDimension+":0";
		return libsvm.trim();
	}
	
	public Dimension [] getDimensionArray() {
		return dimensionArray;
	}
	public String toCSVString(int maxDimension) {
		double [] vals = new double[maxDimension];
		for (Dimension d : dimensionArray) {
			vals[d.getDimension()-1] = d.getValue();
		}
		String csv = "";
		for (int i = 0;i < vals.length;i++) {
			csv += (vals[i] + ",");
		}
		// class is fixed at the end
		csv += like;

		return csv;
	}



}
