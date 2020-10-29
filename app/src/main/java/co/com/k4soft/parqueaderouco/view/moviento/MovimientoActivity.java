package co.com.k4soft.parqueaderouco.view.moviento;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.com.k4soft.parqueaderouco.R;
import co.com.k4soft.parqueaderouco.entidades.Movimiento;
import co.com.k4soft.parqueaderouco.entidades.Tarifa;
import co.com.k4soft.parqueaderouco.persistencia.room.DataBaseHelper;
import co.com.k4soft.parqueaderouco.utilities.ActionBarUtil;

public class MovimientoActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private ActionBarUtil actionBarUtil;
    @BindView(R.id.txtPlaca)
    public EditText txtPlaca;
    @BindView(R.id.tipoTarifaSpinner)
    public  Spinner tipoTarifaSpinner;
    @BindView(R.id.btnIngreso)
    public Button btnIngreso;
    @BindView(R.id.btnSalida)
    public Button btnSalida;
    @BindView(R.id.layoutDatos)
    public ConstraintLayout layoutDatos;
    @BindView(R.id.totalHoras)
    public TextView totalHoras;
    @BindView(R.id.txtTotal)
    public TextView txtTotal;
    private List<Tarifa> listaTarifas;
    private Movimiento movimiento;
    private Tarifa tarifa;
    private String[] arrayTarifas;
    private static final String TAG = "Movimiento";
    private String FechaSalida;
    private double TotalPagar;
    private int idMovimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento);
        ButterKnife.bind(this);
        initComponents();
        hideComponents();
        cargarSpinner();
        spinnerOnItemSelected();
    }

    private void spinnerOnItemSelected() {
        tipoTarifaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tarifa = listaTarifas.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void cargarSpinner() {
        listaTarifas = db.getTarifaDAO().listar();
        if(listaTarifas.isEmpty()){
            Toast.makeText(getApplication(),R.string.sin_tarifas,Toast.LENGTH_SHORT).show();
            finish();
        }else{
            arrayTarifas = new String[listaTarifas.size()];
            for(int i = 0; i < listaTarifas.size(); i++){
                arrayTarifas[i] = listaTarifas.get(i).getNombre();
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,arrayTarifas);
            tipoTarifaSpinner.setAdapter(arrayAdapter);

        }
    }

    private void hideComponents() {
        tipoTarifaSpinner.setVisibility(View.GONE);
        btnIngreso.setVisibility(View.GONE);
        btnSalida.setVisibility(View.GONE);
        layoutDatos.setVisibility(View.GONE);
    }

    private void initComponents() {
        db = DataBaseHelper.getDBMainThread(this);
        actionBarUtil = new ActionBarUtil(this);
        actionBarUtil.setToolBar(getString(R.string.registrsr_ingreso_salida));
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void buscarPlaca(View view) {
        hideComponents();
        movimiento = db.getMovimientoDAO().findByPLaca(txtPlaca.getText().toString());
        if(movimiento == null){
            showComponentesIngreso();
        }else{
            showComponentesSalida();
            calcularTotal(movimiento);
        }
    }

    private void showComponentesSalida() {
        btnSalida.setVisibility(View.VISIBLE);
        layoutDatos.setVisibility(View.VISIBLE);
    }

    private void showComponentesIngreso() {
        tipoTarifaSpinner.setVisibility(View.VISIBLE);
        btnIngreso.setVisibility(View.VISIBLE);

    }

    public void registrarIngreso(View view) {
        if(tarifa == null){
            Toast.makeText(getApplicationContext(),R.string.debe_seleccionar_tarifa, Toast.LENGTH_SHORT).show();
        }else if(movimiento == null){
            movimiento = new Movimiento();
            movimiento.setPlaca(txtPlaca.getText().toString());
            movimiento.setIdTarifa(tarifa.getIdTarifa());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            movimiento.setFechaEntrada(currentDateandTime);
            movimiento.setTotalPagado(0);
            new InsercionMoviento().execute(movimiento);
            movimiento = null;
            hideComponents();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void calcularTotal(Movimiento movimiento) {
        String fechaIngreso = movimiento.getFechaEntrada();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaSalida = sdf.format(new Date());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int TotalHoras = 0;

        try {
            Date startDate = simpleDateFormat.parse(fechaIngreso);
            Date endDate = simpleDateFormat.parse(fechaSalida);
            float difference = endDate.getTime() - startDate.getTime();
            float milli = 3600000;
            int horas = (int) Math.ceil(difference / milli);
            TotalHoras = horas;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        FechaSalida = fechaSalida;
        totalHoras.setText(String.valueOf(TotalHoras));
        tarifa = db.getTarifaDAO().findByTarifa(movimiento.getIdTarifa());
        idMovimiento = movimiento.getIdMovimiento();
        double TotalPago = TotalHoras * tarifa.getPrecio();
        TotalPagar = TotalPago;
        txtTotal.setText(String.valueOf(TotalPago) + " Pesos");
    }

    public void registrarSalida(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirmDialogMessage)
                .setTitle(R.string.confirmDialogTitle)
                .setPositiveButton(R.string.confirmDelete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.getMovimientoDAO().updateSalida(FechaSalida, TotalPagar, idMovimiento);
                        Toast.makeText(getApplicationContext(), "La salida se registro exitosamente!", Toast.LENGTH_SHORT).show();
                        hideComponents();
                        txtPlaca.setText("");
                    }
                })
                .setNegativeButton(R.string.cancelDelete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // CANCEL
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    private class InsercionMoviento extends AsyncTask<Movimiento, Void,Void> {

        @Override
        protected Void doInBackground(Movimiento... movimientos) {
            DataBaseHelper.getSimpleDB(getApplicationContext()).getMovimientoDAO().insert(movimientos[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(),R.string.informacion_guardada_exitosamente, Toast.LENGTH_SHORT).show();
        }
    }

}
