package software.kuukkel.fi.recipics;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.net.Uri;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;
//Based on: https://thepseudocoder.wordpress.com/2011/10/05/android-page-swiping-using-viewpager/

/**
 * The <code>ViewPagerFragmentActivity</code> class is the fragment activity hosting the ViewPager
 * @author mwho
 */
public class ViewPagerFragmentActivity extends FragmentActivity
        implements TagFragment.PreserveTags, CameraFragment.PreserveFileUris {

    List<com.cunoraz.tagview.Tag> chosenTags;
    List<com.cunoraz.tagview.Tag> otherTags;
    List<Fragment> fragments;
    Recipe recipe;
    /** maintains the pager adapter*/
    private PagerAdapter mPagerAdapter;
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.fragment_view_pager);

        recipe = new Recipe();
        ArrayList<Uri> pictureUris = new ArrayList<>();
        recipe.setPictureUris(pictureUris);
        //initialse the pager
        this.initialisePaging();
    }

    /**
     * Initialise the fragments to be paged
     */
    private void initialisePaging() {

        fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, CameraFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, RecipeDetailsFillFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, TagFragment.class.getName()));
        this.mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        //
        ViewPager pager = (ViewPager)super.findViewById(R.id.viewpager);
        pager.setAdapter(this.mPagerAdapter);
    }

    public void saveRecipe(View view) {
        CameraFragment cameraFrag = (CameraFragment) fragments.get(0);
        ArrayList<Uri> pictureUris = cameraFrag.getPictureUris();
        if(pictureUris.size() == 0) {
            CharSequence text = "You can't save a recipe without a picture!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
            return;
        }
        recipe.setPictureUris(pictureUris);

        RecipeDetailsFillFragment recipeFrag = (RecipeDetailsFillFragment) fragments.get(1);
        Recipe tmpRecipe = recipeFrag.getRecipe();
        recipe.setName(tmpRecipe.getName());
        recipe.setNotes(tmpRecipe.getNotes());
        recipe.setSource(tmpRecipe.getSource());
        recipe.setStarred(tmpRecipe.isStarred());

        TagFragment tagFrag = (TagFragment) fragments.get(2);
        List<com.cunoraz.tagview.Tag> tmpTags = tagFrag.getChosenTags();
        ArrayList<Tag> tags = new ArrayList<>();
        for(com.cunoraz.tagview.Tag tmpTag: tmpTags) {
            tags.add(new Tag(tmpTag));
        }

        DBHelper db = new DBHelper(this);
        db.insertRecipe(recipe, tags);
    }

    public void SaveChosenTags(List<com.cunoraz.tagview.Tag> chosen, List<com.cunoraz.tagview.Tag> notChosen) {
        chosenTags = chosen;
        otherTags = notChosen;
    }

    public List<List<com.cunoraz.tagview.Tag>> GetTags() {
        if(chosenTags == null || otherTags == null ) {
            chosenTags = new ArrayList<>();
            otherTags = new ArrayList<>();

            DBHelper db = new DBHelper(this);
            ArrayList<Tag> tags = db.getAllTags();
            for (Tag t : tags) {
                otherTags.add(t.getTag());
            }
        }
        List<List<com.cunoraz.tagview.Tag>> tags = new ArrayList<>();
        tags.add(chosenTags);
        tags.add(otherTags);
        return tags;
    }

    public void savePictureUris(ArrayList<Uri> uris) { recipe.setPictureUris(uris); }

    public ArrayList<Uri> getPictureUris() { return recipe.getPictureUris(); }

}