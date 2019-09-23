package com.abc.java9.processmanager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Controller implements Initializable {
	
	@FXML 
	TableView<ProcessHandle> processView;
	final private ObservableList<ProcessHandle> processList=FXCollections.observableArrayList();
	private ProcessListUpdater processListUpdater = new ProcessListUpdater(); 
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		processListUpdater = new ProcessListUpdater();
		processListUpdater.start();
		
		processView.setItems(processList);
		processList.setAll(ProcessHandle.allProcesses().collect(Collectors.toList()));
		
		processView.getColumns().setAll(
				createTableColumn("Command", 250, p->p.info().command().map(Controller::afterLast).orElse("<unknown>")),
				createTableColumn("PID", 75, p->p.pid()),
				createTableColumn("Status", 150, p->p.isAlive()?"Running":"NotRunning"),
				createTableColumn("Owner", 150, p->p.info().user().map(Controller::afterLast).orElse("<unknown>")),
				createTableColumn("Arguments", 75, p->p.info().arguments().stream().map(i -> i.toString()).collect(Collectors.joining(", "))));
	}
	
	private <T> TableColumn<ProcessHandle, T> createTableColumn(
			String header, int width, Function<ProcessHandle, T> function){
		TableColumn<ProcessHandle, T> column = new TableColumn<>(header);
		column.setMinWidth(width);
		column.setCellValueFactory(data -> new SimpleObjectProperty<T>(function.apply(data.getValue())));
		return column;
	}
	
	private static String afterLast(String string) {
		int index=string.lastIndexOf(File.separator);
		return index> -1 ? string.substring(index+1) : string;
	}
	
	@FXML
	public void closeApplication(ActionEvent event) {
		processListUpdater.shutdown();
		Platform.exit();
	}
	
	@FXML
	public void killProcessHandler(final ActionEvent event) {
		new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to kill this process?",
					ButtonType.YES, ButtonType.NO)
		.showAndWait().filter(button->button==ButtonType.YES)
		.ifPresent(response->{
			ProcessHandle selectedItem=processView.getSelectionModel().getSelectedItem();
			if(selectedItem!=null) {
				selectedItem.destroy();
				processListUpdater.updateList();
			}
		});
	}
	
	@FXML
	public void runProcessHandler(final ActionEvent event) {
		final TextInputDialog inputDialog=new TextInputDialog();
		inputDialog.setTitle("Run command ...");
		inputDialog.setContentText("Command Line: ");
		inputDialog.setHeaderText(null);
		inputDialog.showAndWait().ifPresent(c -> {
			try {
				new ProcessBuilder(c).start();
			} catch (IOException e) {
				new Alert(Alert.AlertType.ERROR, "Error encountered while running your command.").show();
			}
		});
	}
	
	private class ProcessListUpdater extends Thread {
		private volatile boolean running=true;
		
		public ProcessListUpdater() {
			super();
			setDaemon(true);
		}
		
		public void shutdown() {
			running=false;
		}

		@Override
		public void run() {
			while(running) {
				updateList();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// Ignored
				}
			}
		}
		
		public synchronized void updateList() {
			processList.setAll(ProcessHandle.allProcesses().collect(Collectors.toList()));
			processView.sort();
		}
	}
	
}
