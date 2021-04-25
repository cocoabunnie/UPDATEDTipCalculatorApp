/*This calculator is meant to help the user calculate how much they should tip depending on the bill and the percentage of their choice
They add their amount in the text box, and then they use the seekBar to slide it across the screen and indicate what percentage of the
bill they'd like to tip

Other features of this app include a "Reset" button and 5%, 10%, 15% and 20% button shortcuts since these are common percentages people use to tip

~*~ VERSION 2 FEATURES ~*~
App can now split the bill among people and tell the user how much each person has to pay. You can also share the information with your friends
via text or email in the app of your choice.
You also have the choice to round off either the tip or the total to the nearest dollar if the user only wants an estimate
*/
package com.example.tipcalculator_miniproject1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast; // creates messages for the user

import static android.Manifest.permission.CALL_PHONE;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private int percentage = 0; //this number will be the percentage shown on the progress bar
    private int splitNum; //how many people do we split the bill with? (user chooses with spinner)
    private TextView percentDisplay; //creating variable to represent the percent display TextView
    private EditText userInputAmount;
    private TextView userTip;
    private TextView userTotal;
    private TextView perPerson;
    private SeekBar seekBar; //This represents the draggable seekBar
    private Spinner peopleSpinner; //User can choose how many people they're splitting the bill with
    private RadioGroup roundChoices; //Three choices user can choose ("No" "Tip" and "Total")
    private RadioButton chosenButton; //Whatever the user chooses out of the three choices
    //Variables to keep track of the users choice of rounding
    private boolean noRounding;
    private boolean tipRounding;
    private boolean totalRounding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noRounding = false;
        tipRounding = false;
        totalRounding = false;

        perPerson = (TextView)findViewById(R.id.perPersonAmount);
        roundChoices = (RadioGroup)findViewById(R.id.RadioChoices);
        peopleSpinner = (Spinner)findViewById(R.id.numberOfPeople);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numPeople, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        peopleSpinner.setAdapter(adapter);
        peopleSpinner.setOnItemSelectedListener(this);
        percentDisplay = (TextView)findViewById(R.id.percentText); //Assigning percent display to it's TextView box (next to the slider)
        seekBar = (SeekBar)findViewById(R.id.userSeekBar);
        userInputAmount = (EditText)findViewById(R.id.userInput); //Assigning userInputAmount to the box the user types in at the top of the app
        userTip = (TextView)findViewById(R.id.tipAmount); //This is the box that displays the tip
        userTotal = (TextView)findViewById(R.id.totalAmount);
        percentDisplay.setText("0%"); //ensuring the percent display starts by displaying "0%" on startup

        //SEEK BAR LISTENER CODE
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { //LISTENERS are for obtaining information on a widget
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { //What happens WHILE the user changes the progress bar
                percentage = progress;
                percentDisplay.setText(Integer.toString(percentage) + "%"); //Updates the percent text next to the seekBar
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { //What happens after the user stops moving the seekBar
                String input = userInputAmount.getText().toString(); //takes user input....
                int barNum = seekBar.getProgress();
                Log.d("TAG", "Inputted: " + input);
                Toast warningMessage = Toast.makeText(getApplicationContext(), "Please add a number!", Toast.LENGTH_SHORT); //warning message to the user if they don't input a number

                if (input.isEmpty()){
                    //Checks to make sure the user put an actual number first - PREVENTS CRASHES
                    warningMessage.show();
                }else if (userInputAmount.getText().toString().equals(".")){
                    warningMessage.show();
                }else{
                    //If there's a number, THEN the app does the calculations
                    double inputAsDouble = Double.parseDouble(input); //turns user input into a double
                    double calculatedTip = calculateTip(inputAsDouble, barNum);
                    userTip.setText("$" + String.format("%.2f", calculatedTip)); //Display the calculated tip (formatted to two decimal spaces)
                    double finalTotal = calculateTotal(calculatedTip, inputAsDouble);
                    userTotal.setText("$" + String.format("%.2f", finalTotal)); //Display the total amount (formatted to two decimal spaces)
                    double perPersonTotal = calculatePerPerson(finalTotal);
                    perPerson.setText(("$" + String.format("%.2f", perPersonTotal)));
                }
            }
        });

        if (savedInstanceState != null){ //Makes sure these are preserved in rotation
            userTotal.setText(savedInstanceState.getString("total"));
            userTip.setText(savedInstanceState.getString("tip"));
            perPerson.setText(savedInstanceState.getString("perPerson"));
            splitNum = savedInstanceState.getInt("splitNum");
        }
    }

    //FUNCTIONS TO CALCULATE TIP, TOTAL, AND PER PERSON AMOUNTS
    public double calculateTip(double currAmount, double barPercent){ //This function calculates the percentage tip and returns it as a double
        double bPercent = barPercent/100;
        double tip;
        tip = currAmount * bPercent;

        if (tipRounding == true){ //Checks if user wants the tip rounded first
            tip = Math.ceil(tip);
        }

        return tip;
    }

    public double calculateTotal(double tip, double amount){ //This function adds the initial amount and the tip to show what total amount the person has to pay
        double returnedAmount = tip + amount;

        if (totalRounding == true){ //Checks if user wants total rounded first
            returnedAmount = Math.ceil(returnedAmount);
        }

        return returnedAmount;
    }

    public double calculatePerPerson(double total){
        double personAmount = total/splitNum;

        return personAmount;
    }

    //RESET BUTTON CODE...
    public void resetValues(View view){ //The reset button resets all values back to 0
        userInputAmount.setText("");
        userTip.setText("$0.00");
        userTotal.setText("$0.00");
        perPerson.setText("$0.00");
        seekBar.setProgress(0);
    }

    //SHORTCUT BUTTON CODES
    //~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
    //All shortcut buttons use the shortCutFunction
    public void shortCutFunction(int progress){
        String inputForShortcut = userInputAmount.getText().toString(); //takes user input
        Toast warningMessage = Toast.makeText(getApplicationContext(), "Please add a number!", Toast.LENGTH_SHORT); //warning message to the user

        //Only works if the user types in a number
        if (inputForShortcut.isEmpty()){
            warningMessage.show();
        } else if (inputForShortcut.equals(".")){
            warningMessage.show();
        } else {
            seekBar.setProgress(progress);
            percentDisplay.setText(progress + "%");
            double tip = calculateTip(Double.parseDouble(inputForShortcut), progress);
            userTip.setText("$" + String.format("%.2f", tip)); //Display the calculated tip (formatted to two decimal spaces)
            double total = calculateTotal(tip, Double.parseDouble(inputForShortcut));
            userTotal.setText("$" + String.format("%.2f", total)); //Display the total amount (formatted to two decimal spaces)
            double perPersonTotal = calculatePerPerson(total);
            perPerson.setText(("$" + String.format("%.2f", perPersonTotal)));
        }
    }

    //SHORTCUT BUTTONS CODE
    //Five Percent button
    public void fiveP(View view) {
        shortCutFunction(5);
    }

    //Ten Percent button
    public void tenP(View view){
        shortCutFunction(10);
    }

    //Fifteen Percent button
    public void fifteenP(View view){
        shortCutFunction(15);
    }

    //Twenty Percent button
    public void twentyP(View view){
        shortCutFunction(20);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { //What happens when user chooses the number of people
        String item = parent.getItemAtPosition(position).toString();
        splitNum = Integer.parseInt(item); //updates splitNum variable

        Log.d("PEOPLESELECTED", "You selected " + splitNum + " people.");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //ROUNDING CODE
    public void choosingRoundingOption(View view){ //Code for the radio buttons (How the user would like to round)
        int choice = roundChoices.getCheckedRadioButtonId();
        chosenButton = findViewById(choice);

        if (chosenButton == findViewById(R.id.radioButton1)){ //no rounding
            noRounding = true;
            tipRounding = false;
            totalRounding = false;
        } else if (chosenButton == findViewById(R.id.radioButton2)){ //tip rounding
            noRounding = false;
            tipRounding = true;
            totalRounding = false;
        } else if (chosenButton == findViewById(R.id.radioButton3)){ //total rounding
            noRounding = false;
            tipRounding = false;
            totalRounding = true;
        }
    }

    //CODE FOR OPTIONS MENU ACROSS THE TOP (SHARE AND INFO)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //shows the menu so user can see the two icons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.usermenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //each item menu button
        switch(item.getItemId()){
            case R.id.shareMenuOption: //Share button code
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Just letting you know that the total bill is " + userTotal.getText()
                        + ", the tip for the bill is " + userTip.getText()
                        + ", and the amount per person (your share of the bill) is " + perPerson.getText());
                shareIntent.setType("text/plain");

                Intent showChooserIntent = Intent.createChooser(shareIntent, "ShareWithFriends"); //User can choose what app they would like to use
                try {
                    startActivity(showChooserIntent);
                } catch (ActivityNotFoundException e) {
                    Log.d("OH NO", "We can't find an activity to text!");
                }

                break;

            case R.id.infoMenuOption: //Info button code
                new AlertDialog.Builder(this)
                        .setTitle("Info")
                        .setMessage("Use the drop down menu at the top of the screen if you want to split the bill with more people!")
                        .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    //LIFECYCLE SUPPORT
    @Override
    protected void onSaveInstanceState(Bundle outState) { //Values I want saved when rotating
        super.onSaveInstanceState(outState);
        outState.putString("total", userTotal.getText().toString());
        outState.putString("tip", userTip.getText().toString());
        outState.putString("perPerson", perPerson.getText().toString());
        outState.putBoolean("noR", noRounding);
        outState.putBoolean("tipR", tipRounding);
        outState.putBoolean("totalR", totalRounding);
        outState.putInt("splitNum", splitNum);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { //Values I want restored when finished rotation
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getString("total");
        savedInstanceState.getString("tip");
        savedInstanceState.getString("perPerson");
        savedInstanceState.getBoolean("noR");
        savedInstanceState.getBoolean("tipR");
        savedInstanceState.getBoolean("totalR");
        savedInstanceState.getInt("splitNum");
    }
}