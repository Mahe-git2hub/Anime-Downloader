package application.view.nineanime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.PKIXRevocationChecker.Option;
import java.util.ResourceBundle;

import application.Anime;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class NineAnimeController implements Initializable{

	@FXML
	TableView<Anime> table;
	@FXML
	TableColumn<Anime, String> name;
	@FXML
	TableColumn<Anime, String> status;
	@FXML
	ChoiceBox<String> choicebox;
	@FXML
	TextField animename;
	@FXML
	TextField location;
	@FXML
	Button locationbtn;
	@FXML
	Button downloadbtn;
	@FXML
	Button refreshbtn;
	@FXML
	Label errormsg;
	
	ObservableList<Anime> list = FXCollections.observableArrayList();
	int selected_option = 0;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		name.setCellValueFactory(new PropertyValueFactory<Anime,String>("name"));
		status.setCellValueFactory(new PropertyValueFactory<Anime,String>("status"));
		
		table.setItems(list);
		errormsg.setTextFill(Paint.valueOf("red"));
		
		choicebox.setItems(FXCollections.observableArrayList("Provide Anime Name",
				"Provide URL to a particular episode"));
	
	}
	
	@FXML
	public void pathbtnClick(ActionEvent event)
	{
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select Folder to save Episodes");
		
		Stage primaryStage = (Stage) locationbtn.getScene().getWindow();
		
		File selectedDirectory = directoryChooser.showDialog(primaryStage);
		location.setText(selectedDirectory.toString());
	}
	
	@FXML
	public void refreshAll(){
		
		list.clear();
		table.setItems(list);
		errormsg.setTextFill(Paint.valueOf("red"));
		errormsg.setText("");
		animename.setText("");
		location.setText("");
	}
	
	@FXML
	public void startDownload(ActionEvent event)
	{
		selected_option = choicebox.getSelectionModel().getSelectedIndex()+1;
		
		if(selected_option==0)
		{
			errormsg.setText("Error - Please select an option");
		}
		
		else if(animename.getText().equals(""))
		{
			errormsg.setText("Error - Please set URL or Anime Name");
			//display error
		}
		else if(location.getText().equals(""))
		{
			//display error
			errormsg.setText("Error - Please choose Download Location");
		}
		else if(!list.isEmpty())
		{
			errormsg.setText("Error - Please refresh before you continue");
		}
		else 
		{
			
			errormsg.setTextFill(Paint.valueOf("green"));
			errormsg.setText("Starting Download");
			
			// Actual Downloading here
			// Test with -  http://animeheaven.eu/i.php?a=Bananya
			//http://animeheaven.eu/i.php?a=She%20and%20Her%20Cat%20-%20Everything%20Flows
			
			new Thread(){
				public void run(){
			try {
	            String[] callAndArgs= {"\"python\"","-u","\"scripts\\NineAnimeDownloader.py\"",((Integer)selected_option).toString(),
	            		"\""+animename.getText().trim()+"\"","\""+location.getText().replaceAll("\\\\", "/")+"/\""};
	            Process p = Runtime.getRuntime().exec(callAndArgs);
	            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

	            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	           
	            String s;
	            int i = 1;
	            Anime anime=null;
	            
	            while ((s = stdInput.readLine()) != null) {
	            	
	            	System.out.println(s);
	                
	                if(s.startsWith("Downloading"))
	                {
	                	anime = new Anime(s.substring(s.indexOf(' ')+1),"Downloading");
	                	list.add(anime);
	                	table.setItems(list);
	                }
	                else if(s.startsWith("Downloaded"))
	                {
	                	anime.setStatus("Complete");
	                	anime.setName(s.substring(s.indexOf(' ')+1));
	                	//table.setItems(list);
	                }
	                else {
						
						i=0;
						// create alert for internet connectivity or bad url and exit
	                	Platform.runLater(
			                ()->{
			                	Alert alert = new Alert(AlertType.ERROR);
			    	            alert.setTitle("Error!!!");
			    	            alert.setContentText("Error in downloading. Either your Internet Connection is down ,the URL is broken,the Anime does not exist, or the site is down. Please try again later.");
			    	            alert.showAndWait();
			                });
	    	            
						break;
						//table.setItems(list);
					}
	                i++;
	            }
	            if(i!=0){
	            	anime=new Anime("","");
	            	list.add(anime);
	            	table.setItems(list);
	            	Platform.runLater(
			            ()->{
				            Alert alert = new Alert(AlertType.INFORMATION);
				            alert.setTitle("Task Complete");
				            alert.setContentText("Download Completed. Please click the Refresh Button.");
				            alert.showAndWait();
			            });
		         }
	            while((s=stdError.readLine())!=null)
	            {
	            	Platform.runLater(
		        	  ()->{
		            	Alert alert = new Alert(AlertType.ERROR);
			            alert.setTitle("Error!!!!!!");
			            alert.setContentText("Unknown Error occurred.");
			            alert.showAndWait();
		        	  });
		            break;
	            }
	            
	        }
	        catch (IOException e) {
	        	e.printStackTrace();
	        	Platform.runLater(
		    	  ()->{
		        	Alert alert = new Alert(AlertType.ERROR);
		            alert.setTitle("Error!!!!!!");
		            alert.setContentText("Unknown Error occurred . Closing Application.");
		            alert.showAndWait();
		    	  });
	            System.exit(-1);
	        }
			}}.start();		
		}
	}


}
