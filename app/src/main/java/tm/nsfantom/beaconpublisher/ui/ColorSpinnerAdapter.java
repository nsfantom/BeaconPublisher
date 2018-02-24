package tm.nsfantom.beaconpublisher.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import tm.nsfantom.beaconpublisher.R;

/**
 * Created by user on 2/24/18.
 */

public class ColorSpinnerAdapter extends BaseAdapter {

    enum TagColor {
        CHARCOAL(R.color.tagCharcoal, (byte) 0x01),
        CLOUD(R.color.tagCloud, (byte) 0x02),
        INDIEGOGO(R.color.tagIndigogo, (byte) 0x03),
        KICKSTARTER(R.color.tagKickstarter, (byte) 0x04),
        MU_ORANGE(R.color.tagMuOrange, (byte) 0x05),
        SCARLET(R.color.tagScarlet, (byte) 0x06),
        SKY(R.color.tagSky, (byte) 0x07),
        SMOKE(R.color.tagSmoke, (byte) 0x08);
//	/* do the additions before this entry please */
//                num_tag_colors

        public int colorResource;
        public byte colorCode;

        TagColor(int colorResource, byte colorCode) {
            this.colorResource = colorResource;
            this.colorCode = colorCode;
        }

        static public int getIndex(byte colorCode) {
            TagColor[] aColors = TagColor.values();
            for (int i = 0; i < aColors.length; i++) {
                if (aColors[i].colorCode == colorCode)
                    return i;
            }
            return 0;
        }
    }




    @Override
    public int getCount() {
        return TagColor.values().length;
    }

    @Override
    public Object getItem(int position) {
        return TagColor.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return TagColor.values()[position].colorResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = (LayoutInflater.from(parent.getContext())).inflate(R.layout.color_spinner_items, null);
        convertView.findViewById(R.id.itemHolder).setBackgroundResource(TagColor.values()[position].colorResource);
        ((TextView)convertView.findViewById(R.id.tvColor)).setText(TagColor.values()[position].toString());
        return convertView;
    }
}
