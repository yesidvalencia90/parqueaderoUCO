package co.com.k4soft.parqueaderouco.view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import co.com.k4soft.parqueaderouco.R;
import co.com.k4soft.parqueaderouco.entidades.Movimiento;
import co.com.k4soft.parqueaderouco.entidades.Tarifa;
import co.com.k4soft.parqueaderouco.persistencia.room.DataBaseHelper;
import co.com.k4soft.parqueaderouco.utilities.ActionBarUtil;
import co.com.k4soft.parqueaderouco.view.moviento.MovimientoActivity;

import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReporteActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private static final String TAG = "TEST: ";
    DataBaseHelper db;
    private Movimiento movimiento;
    private Button bntFechaInicio;
    private Button bntFechaFinal;
    private TextView textFechaInicio;
    private TextView textFechaFinal;
    public List<Movimiento> listaMovimiento;
    public ListView listViewReporte;
    private TextView totalRecaudo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);
        initComponents();
        //loadReporte();
        listViewReporte = (ListView) findViewById(R.id.listViewReporte);
        bntFechaInicio = findViewById(R.id.bntFechaInicio);
        bntFechaFinal = findViewById(R.id.bntFechaFinal);
        textFechaInicio = findViewById(R.id.textFechaInicio);
        textFechaFinal = findViewById(R.id.textFechaFinal);
        totalRecaudo = findViewById(R.id.totalRecaudo);
        bntFechaFinal.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                showDatePickerDialogFinal();
            }
        });
        bntFechaInicio.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog(){
        textFechaInicio.setText("");
        textFechaFinal.setText("");
        listViewReporte.setAdapter(null);
        totalRecaudo.setText("");
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showDatePickerDialogFinal(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if(textFechaInicio.getText().toString().isEmpty()){
            if(day < 10 && month < 9){
                textFechaInicio.setText(year + "-0" + (month+1) + "-0" + day);
            }else if(month < 9) {
                textFechaInicio.setText(year + "-0" + (month+1) + "-" + day);
            }else if(day < 10){
                textFechaInicio.setText(year + "-" + (month+1) + "-0" + day);
            }else{
                textFechaInicio.setText(year + "-" + (month+1) + "-" + day);
            }
        }else{
            if(day < 10 && month < 9){
                textFechaFinal.setText(year + "-0" + (month+1) + "-0" + day);
            }else if(month < 9) {
                textFechaFinal.setText(year + "-0" + (month+1) + "-" + day);
            }else if(day < 10){
                textFechaFinal.setText(year + "-" + (month+1) + "-0" + day);
            }else{
                textFechaFinal.setText(year + "-" + (month+1) + "-" + day);
            }
        }
    }

    private void initComponents() {
        db = DataBaseHelper.getDBMainThread(this);
    }

    private void loadReporte() {
        movimiento = db.getMovimientoDAO().findById(1);
        Toast.makeText(getApplicationContext(),"TEST"+movimiento, Toast.LENGTH_SHORT).show();
    }

    public void consultar(View view) {
        String fechaInicio = textFechaInicio.getText().toString();
        String fechaFinal = textFechaFinal.getText().toString();

        if(fechaFinal.isEmpty() || fechaInicio.isEmpty()){
            Toast.makeText(getApplicationContext(),"Selecciona las fechas para continuar.", Toast.LENGTH_SHORT).show();
        }else{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date startDate = simpleDateFormat.parse(fechaInicio + " 00:00:00");
                Date endDate = simpleDateFormat.parse(fechaFinal + " 23:59:59");
                if (endDate.getTime() < startDate.getTime()){
                    Toast.makeText(getApplicationContext(),"La Fecha Final no puede ser menor que la Fecha Inicial: ", Toast.LENGTH_SHORT).show();
                }else{
                    getMovimientos(fechaInicio + " 00:00:00", fechaFinal + " 23:59:59", simpleDateFormat);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void getMovimientos(String FechaInicio, String FechaFinal, SimpleDateFormat simpleDateFormat){
        //db.getMovimientoDAO().updateTest();
        listaMovimiento = db.getMovimientoDAO().getMovimientos(FechaInicio, FechaFinal);
        if (listaMovimiento.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No existen registros para las fechas seleccionadas.", Toast.LENGTH_SHORT).show();
        } else {
            String[] movimientosArray = new String[listaMovimiento.size()];
            int totalRecaudoFechas = 0;
            for (int i = 0; i < listaMovimiento.size(); i++) {
                movimientosArray[i] = "Placa: " + listaMovimiento.get(i).getPlaca() + ", Total: " + listaMovimiento.get(i).getTotalPagado();
                totalRecaudoFechas += listaMovimiento.get(i).getTotalPagado();
            }
            totalRecaudo.setText(String.valueOf(totalRecaudoFechas) + " Pesos");
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, movimientosArray);
            listViewReporte.setAdapter(arrayAdapter);
        }
    }
}