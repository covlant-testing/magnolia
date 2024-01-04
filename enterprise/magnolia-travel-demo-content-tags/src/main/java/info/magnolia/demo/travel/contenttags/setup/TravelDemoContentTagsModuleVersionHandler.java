/**
 * This file Copyright (c) 2017-2024 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.demo.travel.contenttags.setup;

import com.google.common.collect.Sets;
import info.magnolia.contenttags.setup.AddTagsToNodesTask;
import info.magnolia.contenttags.manager.TagManager;
import info.magnolia.dam.jcr.AssetNodeTypes;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Install the travel-demo-content-tags module.
 */
public class TravelDemoContentTagsModuleVersionHandler extends DefaultModuleVersionHandler {

    private final TagManager tagManager;

    @Inject
    public TravelDemoContentTagsModuleVersionHandler(TagManager tagManager) {
        this.tagManager = tagManager;

        register(DeltaBuilder.update("1.4.1", "")
                .addTask(getAddTagsToTourAssetsTask())
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new NodeExistsDelegateTask("Change the template of tour detail", "Change the tour detail component template to include tags.", RepositoryConstants.WEBSITE, "/travel/tour/main/0",
                new ArrayDelegateTask("",
                    new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel/tour/main/0", NodeTypes.Renderable.TEMPLATE, "travel-demo-content-tags:components/tourDetail-content-tags"),
                    new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel/tour/main/0", "tourListLink", "a045a2d0-7b1c-466c-88bc-28352749959c")
                )));
        tasks.add(new ArrayDelegateTask("Add tags to tours", "Tag the tours with set of tags.",
                new AddTagsToNodesTask("tours", Sets.newHashSet("all-inclusive"),
                        Sets.newHashSet("/magnolia-travels/Spectacular-Ammouliani-Island",
                                "/magnolia-travels/Belize-for-Families",
                                "/magnolia-travels/The-Trans-Siberian-Railway",
                                "/magnolia-travels/Antarctic-Active-Adventure",
                                "/magnolia-travels/Hawaii-Five-O",
                                "/magnolia-travels/Dubai-and-Oman",
                                "/magnolia-travels/Jordan-s-Pearls",
                                "/magnolia-travels/Arctic-Archipelagos",
                                "/magnolia-travels/Lapland-for-Families",
                                "/magnolia-travels/Go-Fly-a-Kite"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("city"),
                        Sets.newHashSet("/magnolia-travels/Inside-New-Delhi",
                                "/magnolia-travels/Kyoto",
                                "/magnolia-travels/Dubai-and-Oman",
                                "/magnolia-travels/A-Unique-Basel-Holiday"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("exotic"),
                        Sets.newHashSet("/magnolia-travels/Vietnam--Tradition-and-Today",
                                "/magnolia-travels/A-Taste-of-Malaysia",
                                "/magnolia-travels/Temples-of-the-Orient",
                                "/magnolia-travels/The-Baikal-Experience",
                                "/magnolia-travels/Hawaii-Five-O",
                                "/magnolia-travels/Beach-Paradise-in-Brazil",
                                "/magnolia-travels/Island-hopping-in-Indonesia",
                                "/magnolia-travels/Fabulous-Costa-Rica",
                                "/magnolia-travels/Beautiful-Botswana",
                                "/magnolia-travels/The-Big-African-adventure"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("food"),
                        Sets.newHashSet("/magnolia-travels/Vietnam--Tradition-and-Today",
                                "/magnolia-travels/Inside-New-Delhi",
                                "/magnolia-travels/A-Taste-of-Malaysia",
                                "/magnolia-travels/Last-Bike-out-of-Hanoi",
                                "/magnolia-travels/The-Czech-Republic",
                                "/magnolia-travels/France-for-Families"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("health"),
                        Sets.newHashSet("/magnolia-travels/Hut-to-Hut-in-the-Swiss-Alps",
                                "/magnolia-travels/Grand-Canyon---the-American-South-West",
                                "/magnolia-travels/Dolomites-on-Bike",
                                "/magnolia-travels/The-Long-Trail",
                                "/magnolia-travels/Surfin--Safari",
                                "/magnolia-travels/Hawaii-Five-O",
                                "/magnolia-travels/Go-Fly-a-Kite"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("history"),
                        Sets.newHashSet("/magnolia-travels/West-Coast---Highway-101",
                                "/magnolia-travels/Kyoto",
                                "/magnolia-travels/Temples-of-the-Orient",
                                "/magnolia-travels/The-Trans-Siberian-Railway",
                                "/magnolia-travels/Jordan-s-Pearls",
                                "/magnolia-travels/The-Maya-and-Aztec-Empires",
                                "/magnolia-travels/France-for-Families",
                                "/magnolia-travels/A-Unique-Basel-Holiday"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("islands"),
                        Sets.newHashSet("/magnolia-travels/Spectacular-Ammouliani-Island",
                                "/magnolia-travels/Belize-for-Families",
                                "/magnolia-travels/Surfin--Safari",
                                "/magnolia-travels/Antarctic-Active-Adventure",
                                "/magnolia-travels/Hawaii-Five-O",
                                "/magnolia-travels/Island-hopping-in-Indonesia",
                                "/magnolia-travels/Fabulous-Costa-Rica",
                                "/magnolia-travels/North-Sea-Islands",
                                "/magnolia-travels/Arctic-Archipelagos",
                                "/magnolia-travels/Wood-Water-Stone",
                                "/magnolia-travels/Scuba-Diving-in-Bahamas--famed-Tiger-Beach"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("luxury"),
                        Sets.newHashSet("/magnolia-travels/Spectacular-Ammouliani-Island",
                                "/magnolia-travels/The-Trans-Siberian-Railway",
                                "/magnolia-travels/Dubai-and-Oman"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("mountains"),
                        Sets.newHashSet("/magnolia-travels/Hut-to-Hut-in-the-Swiss-Alps",
                                "/magnolia-travels/Grand-Canyon---the-American-South-West",
                                "/magnolia-travels/Dolomites-on-Bike",
                                "/magnolia-travels/The-Long-Trail"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("sale"),
                        Sets.newHashSet("/magnolia-travels/Hut-to-Hut-in-the-Swiss-Alps",
                                "/magnolia-travels/Inside-New-Delhi",
                                "/magnolia-travels/The-Baikal-Experience",
                                "/magnolia-travels/The-other-side-of-Iran",
                                "/magnolia-travels/North-Sea-Islands",
                                "/magnolia-travels/France-for-Families",
                                "/magnolia-travels/Go-Fly-a-Kite"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("snow"),
                        Sets.newHashSet("/magnolia-travels/The-Trans-Siberian-Railway",
                                "/magnolia-travels/The-Baikal-Experience",
                                "/magnolia-travels/Antarctic-Active-Adventure",
                                "/magnolia-travels/Arctic-Archipelagos",
                                "/magnolia-travels/Lapland-for-Families"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("spirituality"),
                        Sets.newHashSet("/magnolia-travels/Kyoto",
                                "/magnolia-travels/Temples-of-the-Orient",
                                "/magnolia-travels/Last-Bike-out-of-Hanoi",
                                "/magnolia-travels/Island-hopping-in-Indonesia",
                                "/magnolia-travels/The-Czech-Republic"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("unique"),
                        Sets.newHashSet("/magnolia-travels/A-Taste-of-Malaysia",
                                "/magnolia-travels/Temples-of-the-Orient",
                                "/magnolia-travels/The-Baikal-Experience",
                                "/magnolia-travels/The-other-side-of-Iran",
                                "/magnolia-travels/Jordan-s-Pearls",
                                "/magnolia-travels/The-Maya-and-Aztec-Empires",
                                "/magnolia-travels/A-Unique-Basel-Holiday"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("orient"),
                        Sets.newHashSet("/magnolia-travels/The-other-side-of-Iran",
                                "/magnolia-travels/Dubai-and-Oman",
                                "/magnolia-travels/Jordan-s-Pearls"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("drive"),
                        Sets.newHashSet("/magnolia-travels/West-Coast---Highway-101"), tagManager),
                new AddTagsToNodesTask("tours", Sets.newHashSet("wild animals"),
                        Sets.newHashSet("/magnolia-travels/Beach-Paradise-in-Brazil",
                                "/magnolia-travels/Fabulous-Costa-Rica",
                                "/magnolia-travels/Beautiful-Botswana",
                                "/magnolia-travels/Arctic-Archipelagos",
                                "/magnolia-travels/The-Big-African-adventure",
                                "/magnolia-travels/Wood-Water-Stone",
                                "/magnolia-travels/Scuba-Diving-in-Bahamas--famed-Tiger-Beach"), tagManager)));

        tasks.add(getAddTagsToTourAssetsTask());

        return tasks;
    }

    private Task getAddTagsToTourAssetsTask() {
        return new ArrayDelegateTask("Add tags to tour assets", "Tag tour assets with sets of tags.",
                new AddTagsToTourAssetsTask("/tours/shark_brian_warrick_0824.JPG", "reef", "shark", "fish", "sea-life", "coral-reef"),

                new AddTagsToTourAssetsTask("/tours/ash-edmonds-441220-unsplash", "pot", "person", "kitchen"),

                new AddTagsToTourAssetsTask("/tours/ruben-mishchuk-571314-unsplash", "nature", "coast", "ocean", "cove"),

                new AddTagsToTourAssetsTask("/tours/simon-mumenthaler-199501-unsplash", "snow", "winter", "hut", "countryside", "fence"),

                new AddTagsToTourAssetsTask("/tours/paris_eiffel_Louis-Pellissier_IMG_8643.jpg", "building", "urban", "architecture", "crowd", "spire", "tower"),

                new AddTagsToTourAssetsTask("/tours/flickr-surfer-mandolin-373088839_7da451ccc8_b.jpg", "water", "beach", "ocean", "outdoors", "sea", "surfing"),

                new AddTagsToTourAssetsTask("/tours/flicr_iran_ninara_by20_13974556578_ee8d3923c0_k.jpg", "art", "dome", "architecture", "mosque", "arabesque-pattern"),

                new AddTagsToTourAssetsTask("/tours/flickr_czech_Roman-Boed_12033279054_fae78935fe_k.jpg", "weather", "fog", "bridge"),

                new AddTagsToTourAssetsTask("/tours/flickr_dubai_mattharvey1_16087410975_e8e0ce2de8_k.jpg", "building", "skyscraper", "town", "high-rise", "city", "downtown"),

                new AddTagsToTourAssetsTask("/tours/flickr-bike-pierce-martin-9812237694_8112ca89ea_o.jpg", "nature", "landscape", "scenery", "outdoors", "gravel"),

                new AddTagsToTourAssetsTask("/tours/flickr-japan-gate-tasteful_tn-204886404_ab34d73333_o.jpg", "shrine", "worship", "rock", "temple"),

                new AddTagsToTourAssetsTask("/tours/flickr_indonesia_michael_day_2564067897_8d17df6972_b.jpg", "volcano", "plateau", "mountain", "outdoors"),

                new AddTagsToTourAssetsTask("/tours/flickr_costa_rica_SaraYeomans_475571502_a06729c672_o.jpg", "animal", "leaf", "lizard"),

                new AddTagsToTourAssetsTask("/tours/flickr_arctic_roderick_eime_16067347724_bfba426794_k.jpg", "crest", "landscape", "night", "mountain", "scenery"),

                new AddTagsToTourAssetsTask("/tours/flickr_transib_christoph_meier_14763739493_d586e99041_k.jpg", "building", "architecture", "city"),

                new AddTagsToTourAssetsTask("/tours/flickr-new-delhi-paul-hamilton-14646786170_1cf41f94b0_k.jpg", "vehicle", "shop", "motorcycle", "scooter", "bazaar", "market"),

                new AddTagsToTourAssetsTask("/tours/flickr_angkor_chi_king_1071292582_1ed88ac42f_o.jpg", "nature", "sky", "sunset", "dusk", "red-sky"),

                new AddTagsToTourAssetsTask("/tours/flickr_jordan_petra_jcookfisher_13348246373_efe64f0880_k.jpg", "nature", "cave", "light"),

                new AddTagsToTourAssetsTask("/tours/flickr_africa_lions_gabriel_white_4942025852_79fae15c08_o.jpg", "mammal", "grassland", "wildlife", "animal", "lion", "grass"),

                new AddTagsToTourAssetsTask("/tours/flickr_amazon_zach_dischner_by20_13317569555_7d567284b3_k.jpg", "exercise", "working-out", "person", "fitness", "sports"),

                new AddTagsToTourAssetsTask("/tours/flickr_botswana_malcolm_macgregor_6226853168_c4a4be4467_o.jpg", "human", "vessel", "dinghy", "watercraft", "person", "boat"),

                new AddTagsToTourAssetsTask("/tours/flickr_basel_reinald_kirchner_sa20_7700005688_388fc69f8e_k.jpg", "night", "people", "crowd", "fireworks", "ridge"),

                new AddTagsToTourAssetsTask("/tours/flickr-active-biker-dave-h-ridebig-5860471440_7bee981554_o.jpg", "sport", "mountain-bike", "cyclist", "bicycle", "person", "bike"),

                new AddTagsToTourAssetsTask("/tours/flickr_christoph_meier_baikal_boat_14631748310_70ed1f03ae_k.jpg", "vessel", "transportation", "watercraft", "rowboat", "boat"),

                new AddTagsToTourAssetsTask("/tours/flickr_antarctica_Christopher_Michel_8370647648_a57a86bab4_o.jpg", "nature", "snow", "iceberg", "ice", "arctic"),

                new AddTagsToTourAssetsTask("/tours/vietnam_jan_16323513143_82062f3a9a_k.jpg", "tree", "plant", "human", "sunlight"),

                new AddTagsToTourAssetsTask("/tours/flickr_beach_greece_horia_varlan_by20_4332387580_dc593654a3_o.jpg", "nature", "water", "beach", "coast", "sea"),

                new AddTagsToTourAssetsTask("/tours/flickr_mayan_tikal18_graeme_churchard_5975034842_86140bfe7a_b.jpg", "nature", "plant", "jungle", "forest"),

                new AddTagsToTourAssetsTask("/tours/flickr_lapland_sleddogs_guido_da_rozze_5506836395_864f4ec955_o.jpg", "clothing", "dogsled", "vest", "person", "sled"),

                new AddTagsToTourAssetsTask("/tours/flickr-family-hands-woodleywonderworks-2397012858_277b3f403a_o.jpg", "human", "people", "soil", "person", "hand"),

                new AddTagsToTourAssetsTask("/tours/flickr_vancouver_kayak_zack_kuzins_nd20_4770124492_0d2dda7082_o.jpg", "vessel", "kayak", "canoe", "watercraft", "boat"),

                new AddTagsToTourAssetsTask("/tours/flickr_hanoi_motorbike_by20_Khanh_Hmoong_15358008654_4081834552_k.jpg", "dirt-road", "road", "mist", "gravel", "fog"),

                new AddTagsToTourAssetsTask("/tours/flickr_kitesurfing_toby_charlton-taylor_nd20_6451407677_4c8335f95d_o.jpg", "nature", "ocean", "outdoors", "sea"),

                new AddTagsToTourAssetsTask("/tours/flickr-grand-canyon-grand-canyon-national-park-12094069375_881928ee56_k.jpg", "path", "rock", "trail", "valley", "wilderness", "canyon"),

                new AddTagsToTourAssetsTask("/tours/flickr_dolphins_Tony-Malkevist-for-the-Israeli-Ministry-of-Tourism_15391214725_e6cdb14869_k.jpg", "water", "diver", "snorkeling", "diving", "swimming"),

                new AddTagsToTourAssetsTask("/tours/flickr_halligen_jaym_s_14844840920_12546a2afe_k.jpg", "nature", "weather", "cloud", "sky", "water")
        );
    }

    private class AddTagsToTourAssetsTask extends NodeExistsDelegateTask {

        private AddTagsToTourAssetsTask(String path, String... tags) {
            super("Add tags to " + path, "", DamConstants.WORKSPACE, path,
                    new ArrayDelegateTask("", "",
                            new AddTagsToNodesTask(DamConstants.WORKSPACE, Sets.newHashSet(tags), Sets.newHashSet(path), tagManager),
                            new SetPropertyTask("", DamConstants.WORKSPACE, path, AssetNodeTypes.Asset.LAST_TAGGING_ATTEMPT_DATE, Calendar.getInstance())
            ));
        }
    }
}
