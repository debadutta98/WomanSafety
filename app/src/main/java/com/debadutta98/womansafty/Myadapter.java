package com.debadutta98.womansafty;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.skydoves.powermenu.CircularEffect;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.util.ArrayList;
import java.util.HashMap;

import io.paperdb.Paper;

public class Myadapter extends RecyclerView.Adapter<Myadapter.Holder> {
    ArrayList<String> arrayList1;
ArrayList<String> arrayList2;
Context context;
PowerMenu  powerMenu;
    public Myadapter(ArrayList<String> read1,ArrayList<String> read2, Context context)
    {
     arrayList1=read1;
     arrayList2=read2;
     this.context=context;
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.contact,parent,false);
        return new Holder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position)
    {
holder.textView1.setText(arrayList1.get(position));
holder.textView2.setText(arrayList2.get(position));
holder.imageView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
deleteContact(holder.imageView,position);
    }
});
    }

    private void deleteContact(View view,int position) {
     powerMenu = new PowerMenu.Builder(context)
                // .addItemList(list) // list has "Novel", "Poerty", "Art"
                .addItem(new PowerMenuItem("Delete", false)) // add an item.
                .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
                .setMenuRadius(10f) // sets the corner radius.
                .setMenuShadow(10f) // sets the shadow.
                .setTextColor(ContextCompat.getColor(context,R.color.black))
                .setTextGravity(Gravity.CENTER)
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ContextCompat.getColor(context, R.color.appcolor))
                .setCircularEffect(CircularEffect.BODY)
                .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int position, PowerMenuItem item) {
                   //     Toast.makeText(context,item.getTitle(),Toast.LENGTH_SHORT).show();
                        String cn=arrayList1.get(position);
                        String cc=arrayList2.get(position);
                        arrayList1.remove(position);
                        arrayList2.remove(position);
                        ArrayList<String> a1=Paper.book().read(Cache.contactsname);
                        ArrayList<String> a2=Paper.book().read(Cache.contactsnumber);
                        a1.remove(cn);
                        a2.remove(cc);
                        Paper.book().write(Cache.contactsname,a1);
                        Paper.book().write(Cache.contactsnumber,a2);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, arrayList1.size());
                        powerMenu.dismiss();
                    }
                })
                .build();
        powerMenu.showAsDropDown(view);
    }

    @Override
    public int getItemCount()
    {
        if(arrayList1!=null)
        {
         return arrayList1.size();
        }
        else
        {
            return 0;
        }
    }
    class Holder extends RecyclerView.ViewHolder
    {
TextView textView1,textView2;
ImageView imageView;
        public Holder(@NonNull View itemView)
        {
            super(itemView);
            textView1=(TextView)itemView.findViewById(R.id.contactname);
            imageView=(ImageView)itemView.findViewById(R.id.delete);
            textView2=(TextView)itemView.findViewById(R.id.contactnumber);
        }
    }

}
