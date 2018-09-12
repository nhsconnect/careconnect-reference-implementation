package uk.nhs.careconnect.ri.database.entity.episode;

import uk.nhs.careconnect.ri.database.entity.BaseIdentifier;

import javax.persistence.*;


@Entity
@Table(name="EpisodeOfCareIdentifier", uniqueConstraints= @UniqueConstraint(name="PK_EPISODE_IDENTIFIER", columnNames={"EPISODE_IDENTIFIER_ID"})
		,indexes =
		{
				@Index(name = "IDX_EPISODE_IDENTIFER", columnList="IDENTIFIER_VALUE,SYSTEM_ID")

		})
public class EpisodeOfCareIdentifier extends BaseIdentifier {

	public EpisodeOfCareIdentifier() {

	}

	public EpisodeOfCareIdentifier(EpisodeOfCareEntity episode) {
		this.episode = episode;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name= "EPISODE_IDENTIFIER_ID")
	private Long identifierId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn (name = "EPISODE_ID",foreignKey= @ForeignKey(name="FK_EPISODE_EPISODE_IDENTIFIER"))
	private EpisodeOfCareEntity episode;


    public Long getIdentifierId() { return identifierId; }
	public void setIdentifierId(Long identifierId) { this.identifierId = identifierId; }

	public EpisodeOfCareEntity getEpisode() {
	        return this.episode;
	}

	public void setEpisode(EpisodeOfCareEntity episode) {
	        this.episode = episode;
	}




}
