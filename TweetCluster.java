package Machine_Learning;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.json.JSONObject;

class Tweet{
	private String id;
	private String text;
	private int clusterID;
	
	public Tweet(){
		
	}
	public Tweet(String id,String text){
		this.id = id;
		this.text = text;
	}
	public int getClusterID() {
		return clusterID;
	}
	public void setClusterID(int clusterID) {
		this.clusterID = clusterID;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public static double getDistance(Tweet t1,Tweet t2){
		String []text1 = t1.getText().replaceAll("[^a-zA-Z\\s]", "").split(" ");
		String []text2 = t2.getText().replaceAll("[^a-zA-Z\\s]", "").split(" ");
		
		
		double textLength = 0;
		double common = 0;
		Set<String> s1 = new HashSet<String>();
		Set<String> s2 = new HashSet<String>();
		
		for(String i:text1){
			if(!s1.contains(i)){
				s1.add(i);
				textLength+=1;
			}
			else{
				continue;
			}
		}
		
		for(String j:text2){
			if(!s2.contains(j)){
				s2.add(j);
			}
			else{
				continue;
			}
			if(s1.contains(j)){
				common+=1;
			}
			else{
				textLength+=1;
			}
		}
		double similar = (double)(common/textLength);
		return (double)(1-similar);
	}
}


class Cluster{
	int id;
	Tweet centerid;
	List<Tweet> TweetSet;
	
	public Cluster(int id){
		this.id = id;
		this.centerid = null;
		this.TweetSet = new ArrayList<Tweet>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Tweet getCenterid() {
		return centerid;
	}

	public void setCenterid(Tweet centerid) {
		this.centerid = centerid;
	}

	public List<Tweet> getTweetSet() {
		return TweetSet;
	}

	public void setTweetSet(List<Tweet> tweetSet) {
		TweetSet = tweetSet;
	}
	
	public void addTweetToList(Tweet t1){
		this.TweetSet.add(t1);
	}
	public void clear() {
		TweetSet.clear();
	}
	public void printCluster(){
		System.out.print("Cluster id:"+this.centerid.getId()+" [");
		for(Tweet t:TweetSet){
			System.out.print("("+"Tweet id: "+t.getId()+") ");
		}
		System.out.print("]");
		System.out.println();
	}
}


public class TweetCluster {
	private static List<Tweet> tweets;
	private static List<Cluster> clusters;
	private static int NUM_CLUSTERS = 25;
	
	
	public static void readFile(String jsonPath) throws Exception {
		tweets = new ArrayList<Tweet>();
		FileReader reader = new FileReader(jsonPath);
		BufferedReader input = new BufferedReader(reader);
		String line = "";
		while((line=input.readLine())!=null){
			Tweet t = new Tweet();
			JSONObject jo = new JSONObject(line);
	        //System.out.println("text: " + jo.getString("text")
	         //       + " id: " + jo.getString("id"));
			String text = jo.getString("text");
			String id = jo.getString("id");
			t.setId(id);
			t.setText(text);
			
			tweets.add(t);
		}
		 input.close();
		 reader.close();
	}
	
	// Read initial centroids
		public static List<String> readInitialSeeds(String InitialSeedsPath)
				throws IOException {
			List<String> list = new ArrayList<>();
			FileInputStream fin = new FileInputStream(InitialSeedsPath);
			BufferedReader input = new BufferedReader(new InputStreamReader(fin));
			String line;
			while ((line = input.readLine()) != null) {
				String number[] = line.split(",");
				for(String s : number){
					list.add(s);
				}
			}
			input.close();
			return list;
		}
	
	// create clusters
	public static void creatCluster(List<String> list){
		clusters = new ArrayList<Cluster>();
		for(int i=0;i<NUM_CLUSTERS;i++){
			Cluster c = new Cluster(i);
			c.setCenterid(getTweetById(list.get(i)));
			clusters.add(c);
		}
	}
	
	
	public static Tweet getTweetById(String id){
		for(Tweet t:tweets){
			if(t.getId().equals(id)){
				return t;
			}
		}
		return null;
	}
	
	
	//At first, get all cluster center's information
		public static List<Tweet> getCenter(){
			List<Tweet> centers = new ArrayList<Tweet>(clusters.size());
			for(Cluster c:clusters){
				Tweet centerT = c.getCenterid();
				Tweet t = new Tweet(centerT.getId(),centerT.getText());
				centers.add(t);
			}
			return centers;
		}
		
	
	// Assign clusters to the tweets
	public static void assignclusters() {
		for(Tweet item:tweets){
			double minD = Double.MAX_VALUE;
			int clusterID = 0;
			for (int i = 0; i < clusters.size(); i++) {
				Cluster c = clusters.get(i);
				Tweet ctext = c.getCenterid();
				double distanceToCluster = Tweet.getDistance(ctext, item);
				
				if(distanceToCluster<minD){
					minD = distanceToCluster;
					clusterID = i;
				}
			}
			item.setClusterID(clusterID);
			clusters.get(clusterID).addTweetToList(item);
		}
	}
	
	// Update centroids from the new assigned tweets
	// new center point is the one that has the smallest sum length with other points
		public static void updatecentroids() {
			for(Cluster c:clusters){
				Tweet clusterCenter = new Tweet();
				double minD = Double.MAX_VALUE;
				for(Tweet t:c.TweetSet){
					double totalDistance = 0;
					for(Tweet t1:c.TweetSet){//caculate one point sum distance with other in a cluster
						totalDistance+=Tweet.getDistance(t1, t);
					}
					if(totalDistance<minD){
						minD = totalDistance;
						clusterCenter = t;
					}
				}
				c.setCenterid(clusterCenter);
			}
		}
	
	
		// clears all the tweets from the clusters
		private static void clearClusters() {
			for (Cluster cluster : clusters) {
				cluster.clear();
			}
		}
		
	private static void printclusters() throws IOException {
		for(Cluster c:clusters){
			c.printCluster();
			//System.out.println("Cluster ID:"+c.getId());
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		String jsonPath = "H://FM//src//Machine_Learning//Tweets.json";
		String InitialSeedsPath = "H://FM//src//Machine_Learning//InitialSeeds.txt";
		
		readFile(jsonPath);
//		for(int i=0;i<tweets.size();i++){
//			System.out.println(tweets.get(i).getText());
//		}
		List<String> initialSeeds = readInitialSeeds(InitialSeedsPath);
		creatCluster(initialSeeds);
//		
		//List<Tweet> centerSet = getCenter();
		
		//update until not change
		List<Tweet> firstCenter;
		List<Tweet> afterCenter;
		boolean change = true;
//		do{
//			change = 0;
//			clearClusters();
//			firstCenter = getCenter();
//			assignclusters();
//			updatecentroids();
//			
//			afterCenter = getCenter();
//			for(int i=0;i<afterCenter.size();i++){
//				change+=Tweet.getDistance(firstCenter.get(i), afterCenter.get(i));
//			}
//		}
//		while(change!=0);
		while(change!=false){
			double changeValue = 0;
			clearClusters();
			firstCenter = getCenter();
			assignclusters();
			updatecentroids();
			
			afterCenter = getCenter();
			for(int i=0;i<afterCenter.size();i++){
				changeValue+=Tweet.getDistance(firstCenter.get(i), afterCenter.get(i));
			}
			
			if(changeValue==0){
				change=false;
			}
			else{
				change=true;
			}
		}
		//assignclusters();
		printclusters();

	}

}
