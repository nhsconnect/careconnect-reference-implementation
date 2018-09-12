package uk.nhs.careconnect.ri.database.entity;

import javax.persistence.*;


@Table(name = "Address"
        ,indexes =
        {
                @Index(name = "IDX_ADDRESS_POSTCODE", columnList="postcode")

        })
@Entity
public class AddressEntity extends BaseResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ADDRESS_ID")
    private Long addressId;
    public Long getId() { return this.addressId; }

    @Column(name = "address_1")
    private String address1;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "address_3")
    private String address3;

    @Column(name = "address_4")
    private String address4;

    @Column(name = "address_5")
    private String address5;

    @Column(name = "city")
    private String city;

    @Column(name = "county")
    private String county;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Column(name = "country")
    private String country;

    @Column(name = "postcode")
    private String postcode;

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public String getAddress5() {
        return address5;
    }

    public void setAddress5(String address5) {
        this.address5 = address5;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() { return this.city;}
    public void setCity(String city) { this.city = city; }

    public String getCounty() { return this.county; }
    public void setCounty(String county) { this.county = county; }
}
