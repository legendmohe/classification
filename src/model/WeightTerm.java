package model;

public class WeightTerm {
	public String docNameString;
	public String nameString;
	public double weight;
	public double tf;
	public double idf;
	public double norLength;
	public double norPos;
	
	@Override
	public String toString(){
		return this.nameString + "|weight:" + this.weight 
				+ "|tf:" + this.tf 
				+ "|idf:" + this.idf 
				+ "|norLength:" + this.norLength
				+ "|norPos:" + this.norPos;
	}
}
