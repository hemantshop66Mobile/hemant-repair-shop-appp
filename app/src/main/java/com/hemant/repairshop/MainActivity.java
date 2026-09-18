package com.hemantrepairshop.billing;

import android.app.*; import android.content.*; import android.database.Cursor; import android.graphics.*; import android.graphics.pdf.PdfDocument; import android.net.Uri; import android.os.Bundle; import android.view.*; import android.widget.*;
import androidx.core.content.FileProvider;
import java.io.*; import java.text.*; import java.util.*;

public class MainActivity extends Activity {
    private BillDb db; private LinearLayout list;
    private final SimpleDateFormat date=new SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault());
    @Override public void onCreate(Bundle state){super.onCreate(state);db=new BillDb(this);showHome();}
    private TextView text(String value,int size,int color){TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setPadding(12,10,12,10);return v;}
    private void showHome(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,24,24,12);
        TextView title=text("Hemant Repair Shop",26,Color.WHITE);title.setGravity(Gravity.CENTER);title.setBackgroundColor(Color.rgb(13,71,161));root.addView(title,new LinearLayout.LayoutParams(-1,80));
        Button add=new Button(this);add.setText("+ नया Bill बनाएं");add.setOnClickListener(v->showForm());root.addView(add);root.addView(text("हाल के Bills",20,Color.DKGRAY));
        ScrollView scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);loadBills();
    }
    private EditText field(LinearLayout root,String hint,int inputType){EditText e=new EditText(this);e.setHint(hint);e.setInputType(inputType);root.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;}
    private void showForm(){
        ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(28,24,28,24);scroll.addView(root);root.addView(text("नया Repair Bill",26,Color.rgb(13,71,161)));
        EditText customer=field(root,"Customer का नाम",1),mobile=field(root,"Mobile number",3),device=field(root,"Phone model / device",1),work=field(root,"Repair / काम का विवरण",1),amount=field(root,"कुल रकम ₹",2|8192),notes=field(root,"Notes (optional)",1);
        CheckBox paid=new CheckBox(this);paid.setText("Payment मिल गया");root.addView(paid);Button save=new Button(this);save.setText("Bill Save करें");root.addView(save);Button cancel=new Button(this);cancel.setText("Cancel");cancel.setOnClickListener(v->showHome());root.addView(cancel);
        save.setOnClickListener(v->{if(customer.getText().toString().trim().isEmpty()||amount.getText().toString().trim().isEmpty()){Toast.makeText(this,"नाम और रकम जरूरी है",Toast.LENGTH_SHORT).show();return;}try{long id=db.add(customer.getText().toString(),mobile.getText().toString(),device.getText().toString(),work.getText().toString(),Double.parseDouble(amount.getText().toString()),paid.isChecked(),notes.getText().toString());Toast.makeText(this,"Bill #"+id+" save हो गया",Toast.LENGTH_SHORT).show();showHome();}catch(Exception e){Toast.makeText(this,"रकम सही लिखें",Toast.LENGTH_SHORT).show();}});setContentView(scroll);
    }
    private void loadBills(){
        try(Cursor c=db.all()){if(!c.moveToFirst()){list.addView(text("अभी कोई bill नहीं है।",16,Color.GRAY));return;}do{long id=c.getLong(c.getColumnIndexOrThrow("id")),created=c.getLong(c.getColumnIndexOrThrow("created"));String customer=c.getString(c.getColumnIndexOrThrow("customer")),mobile=c.getString(c.getColumnIndexOrThrow("mobile")),device=c.getString(c.getColumnIndexOrThrow("device")),work=c.getString(c.getColumnIndexOrThrow("work")),notes=c.getString(c.getColumnIndexOrThrow("notes"));double amount=c.getDouble(c.getColumnIndexOrThrow("amount"));boolean paid=c.getInt(c.getColumnIndexOrThrow("paid"))==1;Button card=new Button(this);card.setAllCaps(false);card.setGravity(Gravity.START);card.setText("Bill #"+id+"  •  "+date.format(new Date(created))+"\n"+customer+"  |  "+device+"\n₹"+String.format(Locale.getDefault(),"%.2f",amount)+"  •  "+(paid?"PAID":"DUE")+"\nTap करके PDF share करें");card.setOnClickListener(v->sharePdf(id,created,customer,mobile,device,work,amount,paid,notes));list.addView(card);}while(c.moveToNext());}
    }
    private void sharePdf(long id,long created,String customer,String mobile,String device,String work,double amount,boolean paid,String notes){
        try{PdfDocument doc=new PdfDocument();PdfDocument.Page page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,1).create());Canvas canvas=page.getCanvas();Paint p=new Paint(1);p.setColor(Color.rgb(13,71,161));p.setTextSize(28);p.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText("HEMANT REPAIR SHOP",48,65,p);p.setColor(Color.DKGRAY);p.setTextSize(15);p.setTypeface(Typeface.DEFAULT);int y=105;String[] lines={"REPAIR BILL #"+id,"Date: "+date.format(new Date(created)),"Customer: "+customer,"Mobile: "+mobile,"Device: "+device,"Repair work: "+work,"Notes: "+notes,"Payment: "+(paid?"PAID":"DUE")};for(String line:lines){canvas.drawText(line,48,y,p);y+=32;}p.setTextSize(25);p.setTypeface(Typeface.DEFAULT_BOLD);canvas.drawText("TOTAL: Rs. "+String.format(Locale.US,"%.2f",amount),48,y+28,p);p.setTextSize(13);p.setTypeface(Typeface.DEFAULT);canvas.drawText("Thank you for choosing Hemant Repair Shop",48,780,p);doc.finishPage(page);File dir=new File(getCacheDir(),"bills");dir.mkdirs();File file=new File(dir,"Hemant-Bill-"+id+".pdf");try(FileOutputStream out=new FileOutputStream(file)){doc.writeTo(out);}doc.close();Uri uri=FileProvider.getUriForFile(this,getPackageName()+".files",file);Intent share=new Intent(Intent.ACTION_SEND);share.setType("application/pdf");share.putExtra(Intent.EXTRA_STREAM,uri);share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(share,"Bill share करें"));}catch(Exception e){Toast.makeText(this,"PDF नहीं बन पाया",Toast.LENGTH_LONG).show();}
    }
    @Override public void onBackPressed(){showHome();}
}
