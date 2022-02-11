/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecbexchange;

import static ecbexchange.ECBExchange.DATE_FORMAT_NOW;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 *
 * @author Eleesy
 */
public class MainFormController implements Initializable {
    private ExchangeRates rates;
    private TextField valueDestTextField;
    
    @FXML
    private Label msgLabel;

    @FXML
    private TextField valueATextField;
    
    @FXML
    private ComboBox currencyAComboBox;
    
    @FXML
    private TextField valueBTextField;

    @FXML
    private ComboBox currencyBComboBox;
    
    @FXML
    private CheckBox autoUpdateCheckBox;

    @FXML
    private void handleValueATextFiledAction(ActionEvent event) {
        valueDestTextField = valueBTextField;
        calcDestValue();
    }
    
    @FXML
    private void handleValueBTextFiledAction(ActionEvent event) {
        valueDestTextField = valueATextField;
        calcDestValue();
    }
    
    

    
    /** Calculating destination value / Eredmény számítása */
    public String calcDestValue() {
        String r = null;
        Double srcValue = 0.0;
        Double srcRate = 0.0;
        Double destValue = 0.0;
        Double destRate = 0.0;   
        if (valueDestTextField != null &&
            currencyAComboBox.getValue() != null && !currencyAComboBox.getValue().equals("") && 
            currencyBComboBox.getValue() != null && !currencyBComboBox.getValue().equals("")) {
            if (valueDestTextField.equals(valueBTextField) &&
                valueATextField.getText() != null && !valueATextField.getText().isEmpty()) {
                  srcRate = rates.getRate(currencyAComboBox.getValue());
                  destRate = rates.getRate(currencyBComboBox.getValue());
                  srcValue = Double.valueOf(validDecimalChar(valueATextField.getText()));
            } else 
              if (valueBTextField.getText() != null && !valueBTextField.getText().isEmpty()) {
                  srcRate = rates.getRate(currencyBComboBox.getValue());
                  destRate = rates.getRate(currencyAComboBox.getValue());
                  srcValue = Double.valueOf(validDecimalChar(valueBTextField.getText()));
            }
            destValue = algExchange(srcValue, srcRate, destRate);
            r = String.format("%.2f", destValue);
            valueDestTextField.setText(r);
        }
        return r;
    }
    
    /** Main exchange algorithm / Fő algoritmus */
    private Double algExchange(Double srcValue, Double srcRate, Double destRate) {
        Double destValue;
        destValue = (srcValue / srcRate) * destRate;
        return destValue;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rates = new ExchangeRates();
        configureComboBox(currencyAComboBox);
        configureComboBox(currencyBComboBox);
        currencyAComboBox.setValue("EUR");
        currencyBComboBox.setValue("HUF");
        
        autoUpdateCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (oldValue != newValue) {
                    calcDestValue();
                }
            }
        });         
        
        currencyAComboBox.getSelectionModel().selectedItemProperty().addListener((options,oldValue,newValue) -> {
                if (oldValue != newValue) {
                    calcDestValue();
                }
            }
        );

        currencyBComboBox.getSelectionModel().selectedItemProperty().addListener((options,oldValue,newValue) -> {
                if (oldValue != newValue) {
                    calcDestValue();
                }
            }
        );        

        // force the field to be numeric only
        valueATextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, 
                String newValue) {
                if (!newValue.matches("\\d*")) { // ^\\d
                    valueATextField.setText(newValue.replaceAll("[^0-9^_.^_,]", ""));
                }
            }
        });

        // force the field to be numeric only
        valueBTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, 
                String newValue) {
                if (!newValue.matches("\\d*")) { // ^\\d
                    valueBTextField.setText(newValue.replaceAll("[^0-9^_.^_,]", ""));
                }
            }
        });
        
    }   
    
    public void updateMsgLabel() {
       // msgLabel.setText("Online");
    }

    /** Frissító eljárás */
    public Boolean onUpdateAction() {
        if (autoUpdateCheckBox.isSelected()) {
            if (rates.updateByURL(ECBExchange.DATA_URL)) {
                configureComboBox(currencyAComboBox);
                configureComboBox(currencyBComboBox);
                
                DateFormat df = new SimpleDateFormat(DATE_FORMAT_NOW);
                String data = df.format(new Date());
                msgLabel.setText("    Fissítve: "+data);
                return true;
            }
        }
        return false;
    }
    
    /** Replace ',' to '.' / Kicseréli a ','-őt '.'-ra */ 
    public String validDecimalChar(String inputString) {
       return inputString.replace(",", "."); 
    }
    
    /** Load a ComboBox with set of currency / Feltölt egy ComboBox-ot az árfolyamokkal */ 
    private void configureComboBox(ComboBox cb) {
        ObservableList<String> names = FXCollections.observableArrayList(rates.getCurrencies());
        Object selobj = cb.getValue();
        cb.setItems(names);
        if (names.contains(selobj)) {
            cb.setValue(selobj);
        } else cb.setValue("");       
    }
    
}
